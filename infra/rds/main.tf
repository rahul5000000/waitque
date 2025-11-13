terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    postgresql = {
      source  = "cyrilgdn/postgresql"
      version = "~> 1.22"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  backend "s3" {
    bucket = "waitque-terraform-state-bucket"
    key    = "rds/postgres/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region
}

provider "postgresql" {
  host            = aws_db_instance.postgres.address
  port            = 5432
  username        = var.db_username
  password        = random_password.db_password.result
  sslmode         = "require"
  connect_timeout = 15
  superuser       = false
}

# -------------------------------
# VPC & Networking
# -------------------------------
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Security group for DB access
resource "aws_security_group" "db_sg" {
  name_prefix = "waitque-rds-postgres-sg-"
  description = "Allow DB access"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidrs
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# -------------------------------
# Secrets Manager: DB credentials
# -------------------------------
resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "waitque-rds-postgres-credentials"
  description = "Credentials for RDS PostgreSQL"
}

resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "!@#"
}

resource "aws_secretsmanager_secret_version" "db_credentials_version" {
  secret_id     = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = random_password.db_password.result
  })
}

# -------------------------------
# RDS PostgreSQL Instance
# -------------------------------
resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "waitque-rds-postgres-subnets"
  subnet_ids = data.aws_subnets.default.ids
}

resource "aws_db_instance" "postgres" {
  identifier              = "waitque-postgres-db"
  engine                  = "postgres"
  engine_version          = "16"
  instance_class          = "db.t4g.micro"
  allocated_storage       = 20
  storage_type            = "gp2"
  db_name                 = var.db_name
  username                = var.db_username
  password                = random_password.db_password.result
  multi_az                = false
  backup_retention_period = 7
  deletion_protection     = true
  skip_final_snapshot     = false
  publicly_accessible     = var.db_publicly_accessible
  vpc_security_group_ids  = [aws_security_group.db_sg.id]
  db_subnet_group_name    = aws_db_subnet_group.db_subnet_group.name
  storage_encrypted       = true
  performance_insights_enabled = false

  tags = {
    Environment = var.environment
    Name        = "waitque-rds-postgres-${var.environment}"
  }
}

# -------------------------------
# Init App DBs
# -------------------------------
# Random passwords
resource "random_password" "keycloak_password" {
  length           = 16
  special          = true
  override_special = "!@#$%"
}

resource "random_password" "waitque_password" {
  length           = 16
  special          = true
  override_special = "!@#$%"
}

# Keycloak Database
resource "postgresql_database" "keycloak" {
  name = "keycloak"
}

resource "postgresql_role" "keycloak_user" {
  name     = "keycloak_user"
  login    = true
  password = random_password.keycloak_password.result
}

resource "postgresql_schema" "keycloak" {
  name     = "keycloak"
  database = postgresql_database.keycloak.name
  owner    = postgresql_role.keycloak_user.name

  depends_on = [postgresql_role.keycloak_user]
}

resource "postgresql_grant" "keycloak_db" {
  database    = postgresql_database.keycloak.name
  role        = postgresql_role.keycloak_user.name
  object_type = "database"
  privileges  = ["ALL"]
}

resource "postgresql_default_privileges" "keycloak_tables" {
  database    = postgresql_database.keycloak.name
  role        = postgresql_role.keycloak_user.name
  owner       = postgresql_role.keycloak_user.name
  schema      = postgresql_schema.keycloak.name
  object_type = "table"
  privileges  = ["ALL"]
}

resource "postgresql_default_privileges" "keycloak_sequences" {
  database    = postgresql_database.keycloak.name
  role        = postgresql_role.keycloak_user.name
  owner       = postgresql_role.keycloak_user.name
  schema      = postgresql_schema.keycloak.name
  object_type = "sequence"
  privileges  = ["ALL"]
}

# Waitque Database
resource "postgresql_database" "waitque" {
  name = "waitque"
}

resource "postgresql_role" "waitque_api" {
  name     = "waitque_api"
  login    = true
  password = random_password.waitque_password.result
}

resource "postgresql_schema" "waitque" {
  name     = "waitque"
  database = postgresql_database.waitque.name
  owner    = postgresql_role.waitque_api.name

  depends_on = [postgresql_role.waitque_api]
}

resource "postgresql_grant" "waitque_db" {
  database    = postgresql_database.waitque.name
  role        = postgresql_role.waitque_api.name
  object_type = "database"
  privileges  = ["ALL"]
}

resource "postgresql_default_privileges" "waitque_tables" {
  database    = postgresql_database.waitque.name
  role        = postgresql_role.waitque_api.name
  owner       = postgresql_role.waitque_api.name
  schema      = postgresql_schema.waitque.name
  object_type = "table"
  privileges  = ["ALL"]
}

resource "postgresql_default_privileges" "waitque_sequences" {
  database    = postgresql_database.waitque.name
  role        = postgresql_role.waitque_api.name
  owner       = postgresql_role.waitque_api.name
  schema      = postgresql_schema.waitque.name
  object_type = "sequence"
  privileges  = ["ALL"]
}

# Store credentials in Secrets Manager (same as Option 1)
resource "aws_secretsmanager_secret" "keycloak_credentials" {
  name = "waitque-keycloak-db-credentials-${var.environment}"
}

resource "aws_secretsmanager_secret_version" "keycloak_credentials" {
  secret_id = aws_secretsmanager_secret.keycloak_credentials.id
  secret_string = jsonencode({
    username = postgresql_role.keycloak_user.name
    password = random_password.keycloak_password.result
    database = postgresql_database.keycloak.name
    schema   = postgresql_schema.keycloak.name
    host     = aws_db_instance.postgres.address
    port     = aws_db_instance.postgres.port
  })
}

resource "aws_secretsmanager_secret" "waitque_credentials" {
  name = "waitque-api-db-credentials-${var.environment}"
}

resource "aws_secretsmanager_secret_version" "waitque_credentials" {
  secret_id = aws_secretsmanager_secret.waitque_credentials.id
  secret_string = jsonencode({
    username = postgresql_role.waitque_api.name
    password = random_password.waitque_password.result
    database = postgresql_database.waitque.name
    schema   = postgresql_schema.waitque.name
    host     = aws_db_instance.postgres.address
    port     = aws_db_instance.postgres.port
  })
}

# -------------------------------
# Outputs
# -------------------------------
output "db_endpoint" {
  description = "Waitque RDS PostgreSQL endpoint"
  value       = aws_db_instance.postgres.endpoint
}

output "db_secret_arn" {
  description = "Waitque Secrets Manager secret ARN"
  value       = aws_secretsmanager_secret.db_credentials.arn
}

output "db_address" {
  description = "RDS instance address (hostname only)"
  value       = aws_db_instance.postgres.address
}

output "db_port" {
  description = "RDS instance port"
  value       = aws_db_instance.postgres.port
}

output "db_security_group_id" {
  description = "RDS security group ID"
  value       = aws_security_group.db_sg.id
}

# These assume you created them with the PostgreSQL provider approach
output "keycloak_secret_arn" {
  description = "Keycloak database credentials secret ARN"
  value       = aws_secretsmanager_secret.keycloak_credentials.arn
}

output "waitque_secret_arn" {
  description = "Waitque API database credentials secret ARN"
  value       = aws_secretsmanager_secret.waitque_credentials.arn
}