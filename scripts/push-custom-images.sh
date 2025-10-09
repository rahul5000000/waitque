#!/bin/bash
set -e

# List of modules to build
modules=("user-service" "company-service" "customer-service")

for module in "${modules[@]}"; do
    echo "Building Docker image for $module..."

    # Build Docker image
    docker build -t "$module:latest" -f "$module/Dockerfile" "$module"

    echo "$module image built successfully!"
done
