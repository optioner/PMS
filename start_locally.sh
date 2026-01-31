#!/bin/bash

# Kill running processes if any (simple cleanup)
pkill -f "spring-boot:run"
pkill -f "vite"

echo "Starting PMS locally..."

# Start Backend
echo "Starting Backend (H2 Database)..."
# Use absolute path or relative to project root
./backend/apache-maven-3.9.6/bin/mvn spring-boot:run -f backend/pom.xml > backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend started with PID $BACKEND_PID. Logs: backend.log"

# Start Frontend
echo "Starting Frontend..."
cd frontend
npm run build -- --host > ../frontend.log 2>&1 &


if [ $? -ne 0 ]; then
    echo "Frontend build failed!"
    exit 1
fi

echo ">> Starting Frontend (using npx serve)..."
# Using 'serve' to host the static files on port 3000
nohup npx serve -s dist -l $FRONTEND_PORT > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend started with PID $FRONTEND_PID. Logs: frontend.log"

echo "------------------------------------------------"
echo "Services are starting up!"
echo "Backend API: http://localhost:8080"
echo "Frontend UI: http://localhost:5173"
echo "------------------------------------------------"
echo "To stop servers, run: kill $BACKEND_PID $FRONTEND_PID"
