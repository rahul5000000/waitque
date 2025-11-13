variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

variable "keycloak_admin_password" {
  description = "Keycloak admin password"
  type        = string
  sensitive   = true
}

variable "company_service_client_secret" {
  description = "OAuth client secret for company service"
  type        = string
  sensitive   = true
}

variable "customer_service_client_secret" {
  description = "OAuth client secret for customer service"
  type        = string
  sensitive   = true
}