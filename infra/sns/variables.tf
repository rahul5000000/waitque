variable "topics" {
  description = "Map of SNS topics to create"
  type = map(object({
    display_name                = optional(string)
    fifo_topic                  = optional(bool, false)
    content_based_deduplication = optional(bool, false)
    kms_master_key_id           = optional(string)

    # DLQ config (optional)
    enable_dlq = optional(bool, false)

    # Email subscriptions (optional)
    email_subscriptions = optional(list(string), [])

    tags = optional(map(string), {})
  }))
}

variable "default_tags" {
  description = "Tags applied to all resources"
  type        = map(string)
  default     = {}
}
