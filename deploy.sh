#!/bin/bash

# PMS One-Stop Deployment Script

echo "==========================================="
echo "   Project Management System (PMS) Deploy  "
echo "==========================================="

# Check requirements
if ! command -v docker &> /dev/null; then
    echo "Error: docker is not installed."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "Error: docker-compose is not installed."
    exit 1
fi

# Setup Environment
if [ ! -f .env ]; then
    echo "Creating .env file from .env.example..."
    cp .env.example .env
else
    echo ".env file already exists. Using existing configuration."
fi

# Build and Run
echo "Building and starting services..."
docker-compose down
docker-compose up -d --build

# Status Check
echo "Waiting for services to start..."
sleep 10

if [ "$(docker ps -q -f name=pms-backend)" ] && [ "$(docker ps -q -f name=pms-frontend)" ]; then
    echo "==========================================="
    echo "   Deployment Successful!                  "
    echo "==========================================="
    echo "Frontend: http://localhost"
    echo "Backend:  http://localhost:8080"
    echo "MySQL:    Port 3306"
    echo "Redis:    Port 6379"
    echo "==========================================="
else
    echo "Warning: Services may not have started correctly. Check logs with 'docker-compose logs -f'"
fi
