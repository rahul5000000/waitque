variable "aws_region" {
  description = "AWS region for RDS deployment"
  type        = string
  default     = "us-east-1"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "postgres"
}

variable "db_username" {
  description = "Master username"
  type        = string
  default     = "postgres"
}

variable "allowed_cidrs" {
  description = "List of CIDRs allowed to connect to RDS"
  type        = list(string)
  default     = ["10.0.0.0/16"]
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "prod"
}

variable "db_publicly_accessible" {
  description = "Makes the DB publicly accessible"
  type        = bool
  default     = false
}
