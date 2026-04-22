#!/bin/bash
echo "Starting Smart Grid Backend..."
cd "$(dirname "$0")/backend"
mvn spring-boot:run
