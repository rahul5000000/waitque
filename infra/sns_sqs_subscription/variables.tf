variable "topic_arn" {
  type = string
}

variable "queue_arn" {
  type = string
}

variable "queue_url" {
  type = string
}

variable "raw_message_delivery" {
  type    = bool
  default = true
}
