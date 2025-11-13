#!/bin/bash

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CLUSTER_NAME="waitque-cluster"
AWS_REGION=${AWS_REGION:-us-east-1}

show_help() {
  cat << EOF
${BLUE}Waitque ECS Helper Script${NC}

${YELLOW}Usage:${NC}
  $0 [command] [options]

${YELLOW}Commands:${NC}
  status              Show status of all services
  logs <service>      Tail logs for a service
  restart <service>   Restart a service
  scale <service> <count>  Scale a service
  exec <service>      Execute shell in a running task
  deploy <service>    Deploy new version of a service
  list-tasks          List all running tasks
  describe <service>  Detailed info about a service
  health              Check target group health
  urls                Show all service URLs

${YELLOW}Services:${NC}
  keycloak, user-service, company-service, customer-service, all

${YELLOW}Examples:${NC}
  $0 status
  $0 logs keycloak
  $0 restart user-service
  $0 scale company-service 2
  $0 deploy all
  $0 health

EOF
}

get_services() {
  if [ "$1" == "all" ]; then
    echo "keycloak user-service company-service customer-service"
  else
    echo "$1"
  fi
}

status() {
  echo -e "${YELLOW}📊 Service Status${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

  aws ecs list-services --cluster $CLUSTER_NAME --query 'serviceArns[]' --output text | \
  while read service_arn; do
    service_name=$(echo $service_arn | rev | cut -d'/' -f1 | rev)

    service_info=$(aws ecs describe-services \
      --cluster $CLUSTER_NAME \
      --services $service_name \
      --query 'services[0]' \
      --output json)

    running=$(echo $service_info | jq -r '.runningCount')
    desired=$(echo $service_info | jq -r '.desiredCount')
    status=$(echo $service_info | jq -r '.status')

    if [ "$running" == "$desired" ] && [ "$status" == "ACTIVE" ]; then
      echo -e "${GREEN}✓${NC} $service_name: $running/$desired running"
    else
      echo -e "${RED}✗${NC} $service_name: $running/$desired running (Status: $status)"
    fi
  done
}

logs() {
  local service=$1
  if [ -z "$service" ]; then
    echo -e "${RED}Error: Service name required${NC}"
    echo "Usage: $0 logs <service-name>"
    exit 1
  fi

  local log_group="/ecs/waitque/$service"
  echo -e "${YELLOW}📋 Tailing logs for $service...${NC}"
  echo -e "${BLUE}Log Group: $log_group${NC}"
  echo -e "${BLUE}Press Ctrl+C to stop${NC}"
  echo ""

  aws logs tail $log_group --follow --format short
}

restart() {
  local services=$(get_services $1)

  for service in $services; do
    echo -e "${YELLOW}🔄 Restarting $service...${NC}"
    aws ecs update-service \
      --cluster $CLUSTER_NAME \
      --service $service \
      --force-new-deployment \
      --region $AWS_REGION \
      >/dev/null

    if [ $? -eq 0 ]; then
      echo -e "${GREEN}✓ $service restart initiated${NC}"
    else
      echo -e "${RED}✗ Failed to restart $service${NC}"
    fi
  done
}

scale() {
  local service=$1
  local count=$2

  if [ -z "$service" ] || [ -z "$count" ]; then
    echo -e "${RED}Error: Service and count required${NC}"
    echo "Usage: $0 scale <service-name> <count>"
    exit 1
  fi

  echo -e "${YELLOW}📈 Scaling $service to $count tasks...${NC}"
  aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $service \
    --desired-count $count \
    --region $AWS_REGION \
    >/dev/null

  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ $service scaled to $count${NC}"
  else
    echo -e "${RED}✗ Failed to scale $service${NC}"
  fi
}

exec_shell() {
  local service=$1

  if [ -z "$service" ]; then
    echo -e "${RED}Error: Service name required${NC}"
    exit 1
  fi

  echo -e "${YELLOW}🔍 Finding running task for $service...${NC}"

  task_arn=$(aws ecs list-tasks \
    --cluster $CLUSTER_NAME \
    --service-name $service \
    --desired-status RUNNING \
    --query 'taskArns[0]' \
    --output text)

  if [ -z "$task_arn" ] || [ "$task_arn" == "None" ]; then
    echo -e "${RED}✗ No running tasks found for $service${NC}"
    exit 1
  fi

  echo -e "${GREEN}✓ Found task: ${task_arn##*/}${NC}"
  echo -e "${YELLOW}🚀 Starting ECS Exec session...${NC}"

  aws ecs execute-command \
    --cluster $CLUSTER_NAME \
    --task $task_arn \
    --container $service \
    --interactive \
    --command "/bin/sh"
}

deploy() {
  local services=$(get_services $1)

  echo -e "${YELLOW}🚀 Deploying services...${NC}"

  for service in $services; do
    echo -e "${YELLOW}Building and pushing $service...${NC}"

    # Build the service
    if [ -d "./$service" ]; then
      cd ./$service

      # Build based on project type
      if [ -f "pom.xml" ]; then
        ./mvnw clean package -DskipTests
      elif [ -f "build.gradle" ]; then
        ./gradlew clean build -x test
      fi

      # Build Docker image
      docker build -t $service:latest .

      # Get ECR repo
      REPO=$(cd ../infra/ecs && terraform output -raw ecr_repositories | jq -r ".$service" | tr '-' '_')

      # Login to ECR
      AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
      aws ecr get-login-password --region $AWS_REGION | \
        docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

      # Tag and push
      docker tag $service:latest $REPO:latest
      docker tag $service:latest $REPO:$(date +%Y%m%d-%H%M%S)
      docker push $REPO:latest
      docker push $REPO:$(date +%Y%m%d-%H%M%S)

      cd ..

      echo -e "${GREEN}✓ $service image pushed${NC}"
    else
      echo -e "${YELLOW}⚠ Directory ./$service not found, skipping build${NC}"
    fi

    # Force new deployment
    echo -e "${YELLOW}Deploying $service to ECS...${NC}"
    aws ecs update-service \
      --cluster $CLUSTER_NAME \
      --service $service \
      --force-new-deployment \
      --region $AWS_REGION \
      >/dev/null

    echo -e "${GREEN}✓ $service deployment initiated${NC}"
  done

  echo -e "\n${YELLOW}⏳ Waiting for services to stabilize...${NC}"
  for service in $services; do
    echo -e "${YELLOW}Waiting for $service...${NC}"
    aws ecs wait services-stable \
      --cluster $CLUSTER_NAME \
      --services $service \
      --region $AWS_REGION
    echo -e "${GREEN}✓ $service is stable${NC}"
  done

  echo -e "\n${GREEN}✨ Deployment complete!${NC}"
}

list_tasks() {
  echo -e "${YELLOW}📋 Running Tasks${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

  task_arns=$(aws ecs list-tasks \
    --cluster $CLUSTER_NAME \
    --desired-status RUNNING \
    --query 'taskArns[]' \
    --output text)

  if [ -z "$task_arns" ]; then
    echo -e "${YELLOW}No running tasks${NC}"
    return
  fi

  for task_arn in $task_arns; do
    task_id=$(echo $task_arn | rev | cut -d'/' -f1 | rev)

    task_info=$(aws ecs describe-tasks \
      --cluster $CLUSTER_NAME \
      --tasks $task_arn \
      --query 'tasks[0]' \
      --output json)

    container_name=$(echo $task_info | jq -r '.containers[0].name')
    status=$(echo $task_info | jq -r '.lastStatus')
    health=$(echo $task_info | jq -r '.containers[0].healthStatus // "UNKNOWN"')

    echo -e "${GREEN}Task:${NC} $task_id"
    echo -e "  Service: $container_name"
    echo -e "  Status: $status"
    echo -e "  Health: $health"
    echo ""
  done
}

describe_service() {
  local service=$1

  if [ -z "$service" ]; then
    echo -e "${RED}Error: Service name required${NC}"
    exit 1
  fi

  echo -e "${YELLOW}📊 Service Details: $service${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

  aws ecs describe-services \
    --cluster $CLUSTER_NAME \
    --services $service \
    --output json | jq -r '
    .services[0] |
    "Status: \(.status)
Running Count: \(.runningCount)
Desired Count: \(.desiredCount)
Task Definition: \(.taskDefinition | split("/") | .[-1])
Launch Type: \(.launchType)
Created: \(.createdAt)

Recent Events:
\(.events[:5] | .[] | "  • \(.createdAt | split(".")[0]): \(.message)")
"'
}

check_health() {
  echo -e "${YELLOW}🏥 Target Group Health${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

  cd ../infra/ecs 2>/dev/null || {
    echo -e "${RED}Error: ecs-terraform directory not found${NC}"
    exit 1
  }

  for service in keycloak user-service company-service customer-service; do
    tg_name="waitque-${service}-tg"

    tg_arn=$(aws elbv2 describe-target-groups \
      --names $tg_name \
      --query 'TargetGroups[0].TargetGroupArn' \
      --output text 2>/dev/null)

    if [ -z "$tg_arn" ] || [ "$tg_arn" == "None" ]; then
      echo -e "${YELLOW}⚠ $service: Target group not found${NC}"
      continue
    fi

    health=$(aws elbv2 describe-target-health \
      --target-group-arn $tg_arn \
      --query 'TargetHealthDescriptions[0].TargetHealth.State' \
      --output text 2>/dev/null)

    if [ "$health" == "healthy" ]; then
      echo -e "${GREEN}✓${NC} $service: healthy"
    elif [ "$health" == "unhealthy" ]; then
      echo -e "${RED}✗${NC} $service: unhealthy"
    elif [ "$health" == "initial" ]; then
      echo -e "${YELLOW}⏳${NC} $service: initializing"
    else
      echo -e "${YELLOW}?${NC} $service: $health"
    fi
  done

  cd .. >/dev/null
}

show_urls() {
  cd ../infra/ecs 2>/dev/null || {
    echo -e "${RED}Error: ecs-terraform directory not found${NC}"
    exit 1
  }

  ALB_URL=$(terraform output -raw alb_url 2>/dev/null)

  if [ -z "$ALB_URL" ]; then
    echo -e "${RED}Error: Could not get ALB URL${NC}"
    exit 1
  fi

  echo -e "${YELLOW}🌐 Service URLs${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${GREEN}Load Balancer:${NC}   $ALB_URL"
  echo -e "${GREEN}Keycloak:${NC}        $ALB_URL/auth"
  echo -e "${GREEN}User Service:${NC}    $ALB_URL/api/users"
  echo -e "${GREEN}Company Service:${NC} $ALB_URL/api/companies"
  echo -e "${GREEN}Customer Service:${NC} $ALB_URL/api/customers"

  cd .. >/dev/null
}

# Main
case "$1" in
  status)
    status
    ;;
  logs)
    logs "$2"
    ;;
  restart)
    restart "$2"
    ;;
  scale)
    scale "$2" "$3"
    ;;
  exec)
    exec_shell "$2"
    ;;
  deploy)
    deploy "$2"
    ;;
  list-tasks)
    list_tasks
    ;;
  describe)
    describe_service "$2"
    ;;
  health)
    check_health
    ;;
  urls)
    show_urls
    ;;
  help|--help|-h)
    show_help
    ;;
  *)
    echo -e "${RED}Unknown command: $1${NC}"
    show_help
    exit 1
    ;;
esac