#!/bin/bash
# GesturGait AI - Start all services
# Run this script to start the ML engine, backend, and mobile app.

echo "=== GesturGait AI ==="
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Start ML Engine
echo "[1/3] Starting ML Engine (FastAPI)..."
cd "$SCRIPT_DIR/ml-engine"
python3 main.py &
ML_PID=$!
echo "  ML Engine PID: $ML_PID (port 8000)"

sleep 2

# Start Backend
echo "[2/3] Starting Backend API (Node/Express)..."
cd "$SCRIPT_DIR/backend"
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
node src/index.js &
BACKEND_PID=$!
echo "  Backend PID: $BACKEND_PID (port 3000)"

sleep 2

# Start Mobile
echo "[3/3] Starting Mobile App (Expo)..."
cd "$SCRIPT_DIR/mobile"
npx expo start &
MOBILE_PID=$!
echo "  Mobile PID: $MOBILE_PID (Expo dev server)"

echo ""
echo "All services started. Press Ctrl+C to stop all."
echo "  ML Engine:  http://localhost:8000"
echo "  Backend:    http://localhost:3000"
echo "  Mobile:     Expo dev tools in browser"
echo ""

trap "kill $ML_PID $BACKEND_PID $MOBILE_PID 2>/dev/null; exit" INT TERM
wait
