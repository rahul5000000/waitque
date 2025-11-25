variable "bucket_name" {
  type        = string
  description = "Name of S3 bucket"
}

variable "expire_noncurrent_days" {
  type        = number
  description = "Days after which non-current object versions expire"
  default     = 1
}

variable "default_root_object" {
  type        = string
  default     = "index.html"
}

variable "force_destroy" {
  type        = bool
  default     = false
}