#!/bin/bash
# Start backend
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=default &
BACKEND_PID=$!
# Wait for backend
sleep 10
# Start frontend dev server
cd ../frontend
npm run dev -- --host 0.0.0.0 &
FRONTEND_PID=$!
echo "Backend PID: $BACKEND_PID (port 8080)"
echo "Frontend PID: $FRONTEND_PID (port 3000)"
echo "Open http://localhost:3000"
wait
