#!/bin/bash

# PMS Host Deployment Script (Non-Docker)
# Use this to deploy directly on the host machine (Linux/macOS)

echo "==========================================="
echo "   PMS Host Deployment (Production Mode)   "
echo "==========================================="

# 1. Check Dependencies
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "Error: '$1' is not installed."
        exit 1
    fi
}

check_command java
check_command mvn
check_command node
check_command npm
check_command mysql

echo ">> Dependencies checked."

# 2. Configuration
# Load .env if exists, otherwise set defaults
if [ -f .env ]; then
    export $(cat .env | grep -v '#' | xargs)
fi

DB_HOST=${DB_HOST:-127.0.0.1}
DB_PORT=${DB_PORT:-3306}
DB_USER=${DB_USER:-root}
DB_PASS=${DB_PASSWORD:-root123456}
SERVER_PORT=${APP_PORT:-8080}
FRONTEND_PORT=3000

echo ">> Database: $DB_HOST:$DB_PORT"

# 3. Stop existing services
echo ">> Stopping existing services..."
pkill -f "pms-backend" || true
pkill -f "serve -s dist" || true
# Kill process on port 8080 if strictly java
lsof -ti:$SERVER_PORT | xargs kill -9 2>/dev/null || true
lsof -ti:$FRONTEND_PORT | xargs kill -9 2>/dev/null || true

# 4. Build and Run Backend
echo "==========================================="
echo "   Building Backend...                     "
echo "==========================================="
cd backend
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Backend build failed!"
    exit 1
fi

echo ">> Starting Backend..."
# Use Spring Profile 'prod' or custom
nohup java -jar target/*.jar \
  --spring.datasource.url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME:-project_management}?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&autoReconnect=true" \
  --spring.datasource.username="$DB_USER" \
  --spring.datasource.password="$DB_PASS" \
  --server.port=$SERVER_PORT \
  > ../backend.log 2>&1 &

BACKEND_PID=$!
echo "Backend started with PID $BACKEND_PID. Logs: backend.log"
cd ..

# 5. Build and Run Frontend
echo "==========================================="
echo "   Building Frontend...                    "
echo "==========================================="
cd frontend
npm install

# Set API URL for production build (CORS allowed in backend)
export VITE_API_BASE_URL="http://localhost:$SERVER_PORT/api/v1"
npm run build

if [ $? -ne 0 ]; then
    echo "Frontend build failed!"
    exit 1
fi

echo ">> Starting Frontend (using npx serve)..."
# Using 'serve' to host the static files on port 3000
nohup npx serve -s dist -l $FRONTEND_PORT > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend started with PID $FRONTEND_PID. Logs: frontend.log"
cd ..

echo "==========================================="
echo "   Deployment Complete!                    "
echo "==========================================="
echo "Frontend: http://localhost:$FRONTEND_PORT"
echo "Backend:  http://localhost:$SERVER_PORT"
echo "logs:     tail -f backend.log"
echo "==========================================="
