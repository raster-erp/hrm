#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/backend"
echo "Starting HRM Backend (Spring Boot) on http://localhost:8080 ..."
./gradlew bootRun
