#!/bin/bash
set -e

# List of modules to build
modules=("user-service" "company-service" "customer-service")

cd "$(dirname "$0")/.."

echo "Compiling modules..."

mvn clean package

echo "Compiled modules!"

for module in "${modules[@]}"; do
    echo "Building Docker image for $module..."

    # Build Docker image
    docker build -t "$module:latest" -f "$module/Dockerfile" "$module"

    echo "$module image built successfully!"
done

docker build -t waitque-keycloak:26.3.2 -f ./keycloak/Dockerfile ./keycloak
