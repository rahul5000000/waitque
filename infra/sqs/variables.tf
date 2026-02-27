variable "name" {
  type = string
}

variable "visibility_timeout_seconds" {
  type    = number
  default = 30
}

variable "message_retention_seconds" {
  type    = number
  default = 345600 # 4 days
}

variable "receive_wait_time_seconds" {
  type    = number
  default = 10
}

variable "enable_dlq" {
  type    = bool
  default = true
}

variable "max_receive_count" {
  type    = number
  default = 5
}

variable "dlq_message_retention_seconds" {
  type    = number
  default = 1209600 # 14 days
}

variable "tags" {
  type    = map(string)
  default = {}
}
