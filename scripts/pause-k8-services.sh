#!/bin/bash
set -e

modules=("user-service" "company-service" "customer-service")

for module in "${modules[@]}"; do
    echo "Pausing pods for $module..."

    kubectl scale deployment $module --replicas=0

    echo "$module pods paused!"
done
