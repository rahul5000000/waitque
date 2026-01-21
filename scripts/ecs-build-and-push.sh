#!/bin/bash
set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🚀 Build and Push Docker Images to ECR${NC}"
echo "========================================"

# Configuration
AWS_REGION=${AWS_REGION:-us-east-1}
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Get ECR repository URLs from Terraform
echo -e "\n${YELLOW}📋 Getting ECR repository URLs...${NC}"
cd ../infra/ecs
USER_SERVICE_REPO=$(terraform output -json ecr_repositories | jq -r '.user_service')
COMPANY_SERVICE_REPO=$(terraform output -json ecr_repositories | jq -r '.company_service')
CUSTOMER_SERVICE_REPO=$(terraform output -json ecr_repositories | jq -r '.customer_service')
KEYCLOAK_REPO=$(terraform output -json ecr_repositories | jq -r '.keycloak')
cd ../../

echo "User Service: $USER_SERVICE_REPO"
echo "Company Service: $COMPANY_SERVICE_REPO"
echo "Customer Service: $CUSTOMER_SERVICE_REPO"
echo "Keycloak: $KEYCLOAK_REPO"

# Login to ECR
echo -e "\n${YELLOW}🔐 Logging into ECR...${NC}"
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Enable buildx
docker buildx create --use || true

# Function to build and push amd64 image
build_and_push() {
  local SERVICE_NAME=$1
  local SERVICE_DIR=$2
  local ECR_REPO=$3
  local DOCKERFILE=${4:-Dockerfile}
  local TAG=$(date +%Y%m%d-%H%M%S)

  echo -e "\n${YELLOW}🔨 Building $SERVICE_NAME...${NC}"

  if [ ! -d "$SERVICE_DIR" ]; then
    echo -e "${RED}❌ Directory $SERVICE_DIR not found${NC}"
    return 1
  fi

  cd $SERVICE_DIR

  echo -e "${YELLOW}📦 Building amd64-only image for $SERVICE_NAME...${NC}"

  docker buildx build \
    --platform linux/amd64 \
    -t $ECR_REPO:latest \
    -t $ECR_REPO:$TAG \
    -f $DOCKERFILE \
    --push \
    .

  echo -e "${GREEN}✅ $SERVICE_NAME pushed successfully${NC}"

  cd ..
}

echo "Compiling modules..."

mvn clean package

echo "Compiled modules!"

# Build and push each service
build_and_push "user-service" "./user-service" "$USER_SERVICE_REPO"
build_and_push "company-service" "./company-service" "$COMPANY_SERVICE_REPO"
build_and_push "customer-service" "./customer-service" "$CUSTOMER_SERVICE_REPO"

# For Keycloak, we will build and push
echo -e "\n${YELLOW}📥 Pulling official Keycloak image...${NC}"
docker buildx build --platform linux/amd64 -t $KEYCLOAK_REPO:latest -f ./keycloak/Dockerfile --push ./keycloak
echo -e "${YELLOW}🏷️  Tagging Keycloak for ECR...${NC}"
docker tag $KEYCLOAK_REPO:latest $KEYCLOAK_REPO:latest

echo -e "\n${GREEN}✅ All images built and pushed successfully!${NC}"
echo -e "\n${YELLOW}Next steps:${NC}"
echo "1. Update ECS task definitions to use the new images"
echo "2. Deploy with: cd ecs-terraform && terraform apply"
