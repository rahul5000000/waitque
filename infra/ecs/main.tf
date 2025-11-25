terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "waitque-terraform-state-bucket"
    key            = "ecs/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region
}

# -------------------------------
# Data Sources
# -------------------------------
data "aws_caller_identity" "current" {}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Get RDS endpoint from existing infrastructure
data "terraform_remote_state" "rds" {
  backend = "s3"
  config = {
    bucket = "waitque-terraform-state-bucket"
    key    = "rds/postgres/terraform.tfstate"
    region = "us-east-1"
  }
}

# -------------------------------
# ECR Repositories
# -------------------------------
resource "aws_ecr_repository" "keycloak" {
  name                 = "waitque/keycloak"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Service     = "keycloak"
  }
}

resource "aws_ecr_repository" "user_service" {
  name                 = "waitque/user-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Service     = "user-service"
  }
}

resource "aws_ecr_repository" "company_service" {
  name                 = "waitque/company-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Service     = "company-service"
  }
}

resource "aws_ecr_repository" "customer_service" {
  name                 = "waitque/customer-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Service     = "customer-service"
  }
}

# -------------------------------
# Security Groups
# -------------------------------
resource "aws_security_group" "alb_sg" {
  name_prefix = "waitque-alb-sg-"
  description = "Security group for ALB"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP from internet"
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS from internet"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "waitque-alb-sg"
    Environment = var.environment
  }
}

resource "aws_security_group" "ecs_sg" {
  name_prefix = "waitque-ecs-sg-"
  description = "Security group for ECS tasks"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
    description     = "Traffic from ALB"
  }

  ingress {
    from_port = 0
    to_port   = 65535
    protocol  = "tcp"
    self      = true
    description = "Allow inter-service communication"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "waitque-ecs-sg"
    Environment = var.environment
  }
}

resource "aws_security_group" "lambda_sg" {
  name        = "lambda-sg"
  description = "Security group for Lambda to talk to ECS services"
  vpc_id      = data.aws_vpc.default.id

  # Lambda needs OUTBOUND traffic to reach ECS tasks
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # No inbound rules needed – Lambda never accepts inbound connections
}

# Allow ECS to access RDS
resource "aws_security_group_rule" "rds_from_ecs" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.rds.outputs.db_security_group_id
  source_security_group_id = aws_security_group.ecs_sg.id
  description              = "PostgreSQL from ECS"
}

# -------------------------------
# Application Load Balancer
# -------------------------------
resource "aws_lb" "main" {
  name               = "waitque-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb_sg.id]
  subnets            = data.aws_subnets.default.ids

  enable_deletion_protection = false
  enable_http2              = true

  tags = {
    Name        = "waitque-alb"
    Environment = var.environment
  }
}

# Target Groups
resource "aws_lb_target_group" "keycloak" {
  name        = "waitque-keycloak-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.default.id
  target_type = "ip"

  health_check {
      enabled             = true
      healthy_threshold   = 2
      unhealthy_threshold = 5
      timeout             = 5
      interval            = 30
      path                = "/health/ready"
      matcher             = "200"
      port                = "9000"
    }

  deregistration_delay = 30

  tags = {
    Name        = "waitque-keycloak-tg"
    Environment = var.environment
  }
}

resource "aws_lb_target_group" "user_service" {
  name        = "waitque-user-service-tg"
  port        = 8084
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.default.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name        = "waitque-user-service-tg"
    Environment = var.environment
  }
}

resource "aws_lb_target_group" "company_service" {
  name        = "waitque-company-service-tg"
  port        = 8082
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.default.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name        = "waitque-company-service-tg"
    Environment = var.environment
  }
}

resource "aws_lb_target_group" "customer_service" {
  name        = "waitque-customer-svc-tg"
  port        = 8083
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.default.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name        = "waitque-customer-service-tg"
    Environment = var.environment
  }
}

# Listeners
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }
}

# Listener Rules
resource "aws_lb_listener_rule" "user_service" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.user_service.arn
  }

  condition {
    path_pattern {
      values = ["/1/*"]
    }
  }
}

resource "aws_lb_listener_rule" "company_service" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 200

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.company_service.arn
  }

  condition {
    path_pattern {
      values = ["/2/*"]
    }
  }
}

resource "aws_lb_listener_rule" "customer_service" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 300

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.customer_service.arn
  }

  condition {
    path_pattern {
      values = ["/3/*"]
    }
  }
}

resource "aws_lb_listener_rule" "keycloak" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 400

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.keycloak.arn
  }

  condition {
    path_pattern {
      values = ["/*", "/realms/*", "/admin/*", "/health*"]
    }
  }
}

# -------------------------------
# ECS Cluster
# -------------------------------
resource "aws_ecs_cluster" "main" {
  name = "waitque-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name        = "waitque-cluster"
    Environment = var.environment
  }
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name = aws_ecs_cluster.main.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 1
    base              = 1
  }
}

# -------------------------------
# IAM Roles
# -------------------------------
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "waitque-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })

  tags = {
    Name        = "waitque-ecs-task-execution-role"
    Environment = var.environment
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_secrets_policy" {
  name = "ecs-secrets-policy"
  role = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "secretsmanager:GetSecretValue"
      ]
      Resource = [
        data.terraform_remote_state.rds.outputs.db_secret_arn,
        data.terraform_remote_state.rds.outputs.keycloak_secret_arn,
        data.terraform_remote_state.rds.outputs.waitque_secret_arn,
        aws_secretsmanager_secret.keycloak_admin.arn,
        aws_secretsmanager_secret.company_service_client_secret.arn,
        aws_secretsmanager_secret.customer_service_client_secret.arn
      ]
    }]
  })
}

resource "aws_iam_role" "ecs_task_role" {
  name = "waitque-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })

  tags = {
    Name        = "waitque-ecs-task-role"
    Environment = var.environment
  }
}

resource "aws_iam_policy" "backend_upload_policy" {
  name = "backend-upload-policy"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect   = "Allow",
      Action   = ["s3:PutObject", "s3:PutObjectAcl"],
      Resource = "${module.waitque-upload-bucket.bucket_arn}/*"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "attach_s3_policy" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.backend_upload_policy.arn
}

# -------------------------------
# Secrets Manager for Application Secrets
# -------------------------------
resource "aws_secretsmanager_secret" "keycloak_admin" {
  name        = "waitque-keycloak-admin-${var.environment}"
  description = "Keycloak admin credentials"
}

resource "aws_secretsmanager_secret_version" "keycloak_admin" {
  secret_id = aws_secretsmanager_secret.keycloak_admin.id
  secret_string = jsonencode({
    username = "admin"
    password = var.keycloak_admin_password
  })
}

resource "aws_secretsmanager_secret" "company_service_client_secret" {
  name        = "waitque-company-service-client-secret-${var.environment}"
  description = "Company service OAuth client secret"
}

resource "aws_secretsmanager_secret_version" "company_service_client_secret" {
  secret_id     = aws_secretsmanager_secret.company_service_client_secret.id
  secret_string = var.company_service_client_secret
}

resource "aws_secretsmanager_secret" "customer_service_client_secret" {
  name        = "waitque-customer-service-client-secret-${var.environment}"
  description = "Customer service OAuth client secret"
}

resource "aws_secretsmanager_secret_version" "customer_service_client_secret" {
  secret_id     = aws_secretsmanager_secret.customer_service_client_secret.id
  secret_string = var.customer_service_client_secret
}

# -------------------------------
# CloudWatch Log Groups
# -------------------------------
resource "aws_cloudwatch_log_group" "keycloak" {
  name              = "/ecs/waitque/keycloak"
  retention_in_days = 7

  tags = {
    Environment = var.environment
    Service     = "keycloak"
  }
}

resource "aws_cloudwatch_log_group" "user_service" {
  name              = "/ecs/waitque/user-service"
  retention_in_days = 7

  tags = {
    Environment = var.environment
    Service     = "user-service"
  }
}

resource "aws_cloudwatch_log_group" "company_service" {
  name              = "/ecs/waitque/company-service"
  retention_in_days = 7

  tags = {
    Environment = var.environment
    Service     = "company-service"
  }
}

resource "aws_cloudwatch_log_group" "customer_service" {
  name              = "/ecs/waitque/customer-service"
  retention_in_days = 7

  tags = {
    Environment = var.environment
    Service     = "customer-service"
  }
}

# -------------------------------
# Service Discovery (for inter-service communication)
# -------------------------------
resource "aws_service_discovery_private_dns_namespace" "main" {
  name        = "waitque.local"
  description = "Service discovery namespace for Waitque services"
  vpc         = data.aws_vpc.default.id

  tags = {
    Name        = "waitque-service-discovery"
    Environment = var.environment
  }
}

resource "aws_service_discovery_service" "keycloak" {
  name = "keycloak"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_service_discovery_service" "company_service" {
  name = "company-service"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_service_discovery_service" "customer_service" {
  name = "customer-service"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

# -------------------------------
# ECS Task Definitions
# -------------------------------
resource "aws_ecs_task_definition" "keycloak" {
  family                   = "waitque-keycloak"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([{
    name  = "keycloak"
    image = "quay.io/keycloak/keycloak:26.3.2"

    command = ["start-dev", "--http-port=8080", "--hostname-strict=false", "--http-relative-path=/", "--http-management-port=9000"]

    portMappings = [{
      containerPort = 8080
      hostPort      = 8080
      protocol      = "tcp"
    },
    {
      containerPort = 9000
      protocol      = "tcp"
    }]

    environment = [
      {
        name  = "KC_DB"
        value = "postgres"
      },
      {
        name  = "KC_DB_URL"
        value = "jdbc:postgresql://${data.terraform_remote_state.rds.outputs.db_address}:5432/keycloak?currentSchema=keycloak"
      },
      {
        name  = "KC_HOSTNAME_STRICT"
        value = "false"
      },
      {
        name  = "KC_PROXY"
        value = "edge"
      },
      {
        name = "KC_HEALTH_ENABLED"
        value = "true"
      },
      {
        name = "KC_HOSTNAME"
        value = aws_lb.main.dns_name
      },
      {
          name = "KC_HOSTNAME_URL"
          value = "http://${aws_lb.main.dns_name}"
        },
      {
          name = "KC_HOSTNAME_ADMIN_URL"
          value = "http://${aws_lb.main.dns_name}"
        },
      {
          name  = "KC_HOSTNAME_STRICT_HTTPS"
          value = "false"
        },
        {
          name  = "KC_HTTP_ENABLED"
          value = "true"
        },
        {
          name  = "KC_HTTPS_CERTIFICATE_FILE"
          value = ""
        },
        {
          name  = "KC_HTTPS_CERTIFICATE_KEY_FILE"
          value = ""
        },
        {
          name  = "KC_HTTPS_REQUIRED"
          value = "none"
        }
    ]

    secrets = [
      {
        name      = "KEYCLOAK_ADMIN"
        valueFrom = "${aws_secretsmanager_secret.keycloak_admin.arn}:username::"
      },
      {
        name      = "KEYCLOAK_ADMIN_PASSWORD"
        valueFrom = "${aws_secretsmanager_secret.keycloak_admin.arn}:password::"
      },
      {
        name      = "KC_DB_USERNAME"
        valueFrom = "${data.terraform_remote_state.rds.outputs.keycloak_secret_arn}:username::"
      },
      {
        name      = "KC_DB_PASSWORD"
        valueFrom = "${data.terraform_remote_state.rds.outputs.keycloak_secret_arn}:password::"
      }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.keycloak.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "exec 3<> /dev/tcp/127.0.0.1/9000; echo -e 'GET /health/ready HTTP/1.1\\r\\nhost: http://localhost\\r\\nConnection: close\\r\\n\\r\\n' >&3; if [ $? -eq 0 ]; then echo 'Healthcheck Successful'; exit 0; else echo 'Healthcheck Failed'; exit 1; fi;"]
      interval    = 30
      timeout     = 5
      retries     = 5
      startPeriod = 240
    }

    deployment_controller = {
      type = "ECS"
    }

    deployment_configuration = {
      minimum_healthy_percent = 50
      maximum_percent         = 200
    }

    health_check_grace_period_seconds = 180
  }])

  tags = {
    Name        = "waitque-keycloak-task"
    Environment = var.environment
  }
}

resource "aws_ecs_task_definition" "user_service" {
  family                   = "waitque-user-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([{
    name  = "user-service"
    image = "${aws_ecr_repository.user_service.repository_url}:latest"

    portMappings = [{
      containerPort = 8084
      protocol      = "tcp"
    }]

    environment = [
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "ecs-${var.environment}"
      },
      {
        name  = "DB_HOST"
        value = data.terraform_remote_state.rds.outputs.db_address
      },
      {
        name  = "KEYCLOAK_ALB_URL"
        value = "http://${aws_lb.main.dns_name}"
      }
    ]

    secrets = [
      {
        name      = "WAITQUE_DB_PASSWORD"
        valueFrom = "${data.terraform_remote_state.rds.outputs.waitque_secret_arn}:password::"
      }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.user_service.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])

  tags = {
    Name        = "waitque-user-service-task"
    Environment = var.environment
  }
}

resource "aws_ecs_task_definition" "company_service" {
  family                   = "waitque-company-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([{
    name  = "company-service"
    image = "${aws_ecr_repository.company_service.repository_url}:latest"

    portMappings = [{
      containerPort = 8082
      protocol      = "tcp"
    }]

    environment = [
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "ecs-${var.environment}"
      },
      {
        name  = "DB_HOST"
        value = data.terraform_remote_state.rds.outputs.db_address
      },
      {
        name  = "KEYCLOAK_ALB_URL"
        value = "http://${aws_lb.main.dns_name}"
      },
      {
        name  = "ALB_URL"
        value = "http://${aws_lb.main.dns_name}"
      }
    ]

    secrets = [
      {
        name      = "WAITQUE_DB_PASSWORD"
        valueFrom = "${data.terraform_remote_state.rds.outputs.waitque_secret_arn}:password::"
      },
      {
        name      = "COMPANY_SERVICE_CLIENT_SECRET"
        valueFrom = aws_secretsmanager_secret.company_service_client_secret.arn
      }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.company_service.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])

  tags = {
    Name        = "waitque-company-service-task"
    Environment = var.environment
  }
}

resource "aws_ecs_task_definition" "customer_service" {
  family                   = "waitque-customer-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([{
    name  = "customer-service"
    image = "${aws_ecr_repository.customer_service.repository_url}:latest"

    portMappings = [{
      containerPort = 8083
      protocol      = "tcp"
    }]

    environment = [
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "ecs-${var.environment}"
      },
      {
        name  = "DB_HOST"
        value = data.terraform_remote_state.rds.outputs.db_address
      },
      {
        name  = "KEYCLOAK_ALB_URL"
        value = "http://${aws_lb.main.dns_name}"
      },
      {
        name  = "ALB_URL"
        value = "http://${aws_lb.main.dns_name}"
      }
    ]

    secrets = [
      {
        name      = "WAITQUE_DB_PASSWORD"
        valueFrom = "${data.terraform_remote_state.rds.outputs.waitque_secret_arn}:password::"
      },
      {
        name      = "CUSTOMER_SERVICE_CLIENT_SECRET"
        valueFrom = aws_secretsmanager_secret.customer_service_client_secret.arn
      }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.customer_service.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])

  tags = {
    Name        = "waitque-customer-service-task"
    Environment = var.environment
  }
}

# -------------------------------
# ECS Services
# -------------------------------
resource "aws_ecs_service" "keycloak" {
  name            = "keycloak"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.keycloak.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = data.aws_subnets.default.ids
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.keycloak.arn
    container_name   = "keycloak"
    container_port   = 8080
  }

  service_registries {
    registry_arn = aws_service_discovery_service.keycloak.arn
  }

  depends_on = [aws_lb_listener.http]

  tags = {
    Name        = "waitque-keycloak-service"
    Environment = var.environment
  }
}

resource "aws_ecs_service" "user_service" {
  name            = "user-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.user_service.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = data.aws_subnets.default.ids
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.user_service.arn
    container_name   = "user-service"
    container_port   = 8084
  }

  depends_on = [
    aws_lb_listener.http,
    aws_ecs_service.keycloak
  ]

  health_check_grace_period_seconds = 240

  tags = {
    Name        = "waitque-user-service"
    Environment = var.environment
  }
}

resource "aws_ecs_service" "company_service" {
  name            = "company-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.company_service.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = data.aws_subnets.default.ids
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.company_service.arn
    container_name   = "company-service"
    container_port   = 8082
  }

  service_registries {
    registry_arn = aws_service_discovery_service.company_service.arn
  }

  depends_on = [
    aws_lb_listener.http,
    aws_ecs_service.keycloak
  ]

  health_check_grace_period_seconds = 240

  tags = {
    Name        = "waitque-company-service"
    Environment = var.environment
  }
}

resource "aws_ecs_service" "customer_service" {
  name            = "customer-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.customer_service.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = data.aws_subnets.default.ids
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.customer_service.arn
    container_name   = "customer-service"
    container_port   = 8083
  }

  service_registries {
    registry_arn = aws_service_discovery_service.customer_service.arn
  }

  depends_on = [
    aws_lb_listener.http,
    aws_ecs_service.keycloak
  ]

  health_check_grace_period_seconds = 240

  tags = {
    Name        = "waitque-customer-service"
    Environment = var.environment
  }
}

# -------------------------------
# S3 Bucket
# -------------------------------

module "waitque-upload-bucket" {
  source = "../s3-cloudfront"

  bucket_name        = "waitque-upload-bucket"
  backend_role_arn   = aws_iam_role.ecs_task_execution_role.arn

  # optional overrides
  expire_noncurrent_days = 7
  default_root_object    = "index.html"
  force_destroy          = false

  # North America geo restriction (these are the defaults anyway)
  geo_whitelist = [
    "US",
    "CA"
  ]
}

# -------------------------------
# Outputs
# -------------------------------
output "alb_dns_name" {
  description = "ALB DNS name"
  value       = aws_lb.main.dns_name
}

output "alb_url" {
  description = "ALB URL"
  value       = "http://${aws_lb.main.dns_name}"
}

output "keycloak_url" {
  description = "Keycloak URL"
  value       = "http://${aws_lb.main.dns_name}"
}

output "ecr_repositories" {
  description = "ECR repository URLs"
  value = {
    keycloak         = aws_ecr_repository.keycloak.repository_url
    user_service     = aws_ecr_repository.user_service.repository_url
    company_service  = aws_ecr_repository.company_service.repository_url
    customer_service = aws_ecr_repository.customer_service.repository_url
  }
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.main.name
}

output "service_discovery_namespace" {
  description = "Service discovery namespace"
  value       = aws_service_discovery_private_dns_namespace.main.name
}

# outputs.tf in VPC stack
output "vpc_id" {
  value = data.aws_vpc.default.id
}

output "vpc_subnet_ids" {
  value = data.aws_subnets.default.ids
}

output "lambda_security_group_id" {
  value = aws_security_group.lambda_sg.id
}