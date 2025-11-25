variable "bucket_name" {
  type        = string
  description = "Name of S3 bucket"
}

variable "backend_role_arn" {
  type        = string
  description = "IAM role ARN that generates presigned PUT URLs"
}

variable "expire_noncurrent_days" {
  type        = number
  description = "Days after which non-current object versions expire"
  default     = 7
}

variable "default_root_object" {
  type        = string
  default     = "index.html"
}

variable "force_destroy" {
  type        = bool
  default     = false
}

variable "geo_whitelist" {
  type = list(string)
  description = "Geo restrictions for CloudFront"
  default = [
    "US", # United States
    "CA" # Canada
  ]
}