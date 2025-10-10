#!/bin/bash
set -e

modules=("user-service" "company-service" "customer-service")

for module in "${modules[@]}"; do
    echo "Restarting pods for $module..."

    kubectl scale deployment $module --replicas=2

    echo "$module pods resumed!"
done
