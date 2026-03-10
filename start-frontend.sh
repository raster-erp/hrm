#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/frontend"
echo "Starting HRM Frontend (Angular) on http://localhost:4200 ..."
npx ng serve --open
