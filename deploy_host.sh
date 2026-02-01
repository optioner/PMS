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

echo "[Step 1] Checking required dependencies..."

echo "  -> Checking if 'java' is installed..."
check_command java

echo "  -> Checking if 'mvn' (Maven) is installed..."
check_command mvn

echo "  -> Checking if 'node' is installed..."
check_command node

echo "  -> Checking if 'npm' is installed..."
check_command npm

echo "  -> Checking if 'mysql' client is installed..."
check_command mysql

echo ">> All dependencies checked successfully."

# 2. Configuration
echo ""
echo "[Step 2] Loading configuration..."

# Load .env if exists, otherwise set defaults
if [ -f .env ]; then
    echo "  -> Found .env file, loading environment variables..."
    export $(cat .env | grep -v '#' | xargs)
else
    echo "  -> No .env file found, using default values..."
fi

echo "  -> Setting DB_HOST (default: 127.0.0.1)..."
DB_HOST=${DB_HOST:-127.0.0.1}

echo "  -> Setting DB_PORT (default: 3306)..."
DB_PORT=${DB_PORT:-3306}

echo "  -> Setting DB_USER (default: root)..."
DB_USER=${DB_USER:-root}

echo "  -> Setting DB_PASSWORD..."
DB_PASS=${DB_PASSWORD:-Knt@06myMUQeKfp4}

echo "  -> Setting SERVER_PORT (default: 8080)..."
SERVER_PORT=${APP_PORT:-8080}

echo "  -> Setting FRONTEND_PORT (default: 3000)..."
FRONTEND_PORT=3000

echo ">> Configuration loaded: Database=$DB_HOST:$DB_PORT, Backend Port=$SERVER_PORT, Frontend Port=$FRONTEND_PORT"

# 3. Stop existing services
echo ""
echo "[Step 3] Stopping existing services..."

echo "  -> Killing any existing 'pms-backend' processes..."
pkill -f "pms-backend" || true

echo "  -> Killing any existing 'serve -s dist' processes..."
pkill -f "serve -s dist" || true

echo "  -> Killing any process on port $SERVER_PORT (backend)..."
lsof -ti:$SERVER_PORT | xargs kill -9 2>/dev/null || true

echo "  -> Killing any process on port $FRONTEND_PORT (frontend)..."
lsof -ti:$FRONTEND_PORT | xargs kill -9 2>/dev/null || true

echo ">> Existing services stopped."

# 4. Build and Deploy Frontend (deploy frontend first as it consumes more resources)
echo ""
echo "==========================================="
echo "[Step 4] Building Frontend...             "
echo "==========================================="

echo "  -> Changing directory to 'frontend/'..."
cd frontend

echo "  -> Running 'npm install' to install dependencies..."
npm install

echo "  -> Removing old 'dist/' directory..."
rm -rf dist

echo "  -> Running 'npm run build' to build frontend assets..."
# Use relative path for API - nginx will proxy to backend
# Do NOT use absolute URL like http://localhost:8080 as it won't work from external IPs
npm run build
if [ $? -ne 0 ]; then
    echo "Frontend build failed!"
    exit 1
fi
echo ">> Frontend build completed successfully."

echo ""
echo "[Step 5] Deploying Frontend with Nginx..."

echo "  -> Copying dist/* to /usr/share/nginx/html/..."
sudo cp -r dist/* /usr/share/nginx/html/

echo "  -> Copying nginx.prod.conf to /etc/nginx/conf.d/default.conf..."
sudo cp nginx.prod.conf /etc/nginx/conf.d/default.conf

echo "  -> Testing nginx configuration and reloading..."
#sudo nginx -t && sudo nginx -s reload

echo "  -> Starting frontend with 'npx serve' on port $FRONTEND_PORT..."
# Using 'serve' to host the static files on port 3000
nohup npx serve -s dist -l $FRONTEND_PORT > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo ">> Frontend started with PID $FRONTEND_PID. Logs: frontend.log"

echo "  -> Returning to project root directory..."
cd ..

# 6. Build and Run Backend
echo ""
echo "==========================================="
echo "[Step 6] Building Backend...              "
echo "==========================================="

echo "  -> Changing directory to 'backend/'..."
cd backend

echo "  -> Running 'mvn clean package -DskipTests' to build the JAR..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Backend build failed!"
    exit 1
fi
echo ">> Backend build completed successfully."

echo ""
echo "  -> Starting Backend application..."
echo "  -> Configuring database connection: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME:-project_management}"
echo "  -> Database user: $DB_USER"
echo "  -> Server port: $SERVER_PORT"
echo "  -> Running backend JAR in background (nohup)..."

# Use Spring Profile 'prod' or custom
nohup java -jar target/*.jar \
  --spring.datasource.url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME:-project_management}?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&autoReconnect=true" \
  --spring.datasource.username="$DB_USER" \
  --spring.datasource.password="$DB_PASS" \
  --server.port=$SERVER_PORT \
  > ../backend.log 2>&1 &

BACKEND_PID=$!
echo ">> Backend started with PID $BACKEND_PID. Logs: backend.log"

echo "  -> Returning to project root directory..."
cd ..

echo ""
echo "==========================================="
echo "   Deployment Complete!                    "
echo "==========================================="
echo "Frontend: http://localhost:$FRONTEND_PORT"
echo "Backend:  http://localhost:$SERVER_PORT"
echo "logs:     tail -f backend.log"
echo "==========================================="
