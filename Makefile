# ============================================================
# WaitQue Integration Test Environment
# ============================================================

DOCKER_COMPOSE = docker compose -f docker-compose.integration.yml --env-file .env.test
PROJECT_NAME = waitque-test

# Colors
GREEN  = \033[0;32m
YELLOW = \033[1;33m
RESET  = \033[0m

# ============================================================
# Commands
# ============================================================

.PHONY: help up down logs test rebuild clean

help:
	@echo ""
	@echo "$(YELLOW)WaitQue Integration Environment Commands:$(RESET)"
	@echo "  make up        - Start all integration test containers"
	@echo "  make down      - Stop and remove containers, volumes, networks"
	@echo "  make logs      - Follow container logs"
	@echo "  make test      - Run integration tests inside Docker"
	@echo "  make rebuild   - Rebuild all images and restart environment"
	@echo "  make clean     - Completely remove everything"
	@echo ""

# ============================================================
# Environment Management
# ============================================================

up:
	@echo "$(GREEN)Starting integration test environment...$(RESET)"
	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) up -d
	@echo "$(GREEN)✅ Environment is up!$(RESET)"
	@$(DOCKER_COMPOSE) ps

down:
	@echo "$(YELLOW)Stopping environment...$(RESET)"
	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) down -v
	@echo "$(GREEN)✅ Environment cleaned up.$(RESET)"

logs:
	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) logs -f

rebuild:
	@echo "$(YELLOW)Rebuilding and restarting containers...$(RESET)"
	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) down -v
	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) build
	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) up -d
	@echo "$(GREEN)✅ Environment rebuilt.$(RESET)"

clean:
	@echo "$(YELLOW)Removing all containers, volumes, and networks...$(RESET)"
	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) down -v --remove-orphans
	@docker network prune -f
	@docker volume prune -f
	@echo "$(GREEN)✅ Cleaned all Docker resources.$(RESET)"

# ============================================================
# Run Integration Tests
# ============================================================

test:
	@echo "$(GREEN)Running integration tests inside Docker network...$(RESET)"
# 	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) up --abort-on-container-exit --build test-runner
	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) up --build test-runner
# 	$(DOCKER_COMPOSE) -p $(PROJECT_NAME) down -v
	@echo "$(GREEN)✅ Tests completed and environment cleaned.$(RESET)"
