#!/bin/bash

# Exit immediately on errors
set -e

# Check for the correct number of arguments
if [ $# -ne 6 ]; then
  echo "Usage: $0 <POSTGRES_USER> <POSTGRES_PASSWORD> <POSTGRES_DB> <KEYCLOAK_ADMIN> <KEYCLOAK_ADMIN_PASSWORD> <COMPANY_SERVICE_SECRET>"
  echo "Example: $0 waitque_api MySuperSecret waitque admin admin123 CompanySecret"
  exit 1
fi

POSTGRES_USER=$1
POSTGRES_PASSWORD=$2
POSTGRES_DB=$3
KEYCLOAK_ADMIN=$4
KEYCLOAK_ADMIN_PASSWORD=$5
COMPANY_SERVICE_SECRET=$6

POSTGRES_SECRET_NAME="postgres-secret"
KEYCLOAK_SECRET_NAME="keycloak-secret"
COMPANY_SERVICE_SECRET_NAME="company-service-secret"

echo "=== Creating Kubernetes secrets ==="
echo "Postgres Secret: $POSTGRES_SECRET_NAME"
echo "  POSTGRES_USER=$POSTGRES_USER"
echo "  POSTGRES_DB=$POSTGRES_DB"
echo
echo "Keycloak Secret: $KEYCLOAK_SECRET_NAME"
echo "  KEYCLOAK_ADMIN=$KEYCLOAK_ADMIN"
echo

# Create Postgres secret
kubectl create secret generic "$POSTGRES_SECRET_NAME" \
  --from-literal=POSTGRES_USER="$POSTGRES_USER" \
  --from-literal=POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
  --from-literal=POSTGRES_DB="$POSTGRES_DB" \
  --dry-run=client -o yaml | kubectl apply -f -

# Create Keycloak secret
kubectl create secret generic "$KEYCLOAK_SECRET_NAME" \
  --from-literal=KEYCLOAK_ADMIN="$KEYCLOAK_ADMIN" \
  --from-literal=KEYCLOAK_ADMIN_PASSWORD="$KEYCLOAK_ADMIN_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

# Create Company Service Client secret
kubectl create secret generic "$COMPANY_SERVICE_SECRET_NAME" \
  --from-literal=COMPANY_SERVICE_SECRET="$COMPANY_SERVICE_SECRET" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "✅ Secrets created or updated successfully."

kubectl apply -f k8s/shared-services/keycloak-realm-config.yaml

echo "✅ Configmaps created or updated successfully."

kubectl apply -f k8s/shared-services

echo "✅ Shared Services started."
