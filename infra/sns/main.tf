# SNS Topics
resource "aws_sns_topic" "this" {
  for_each = var.topics

  name                        = each.key
  display_name                = each.value.display_name
  fifo_topic                  = each.value.fifo_topic
  content_based_deduplication = each.value.content_based_deduplication
  kms_master_key_id           = each.value.kms_master_key_id

  tags = merge(var.default_tags, each.value.tags)
}

# SQS DLQs
resource "aws_sqs_queue" "dlq" {
  for_each = {
    for name, cfg in var.topics :
    name => cfg
    if cfg.enable_dlq
  }

  name = "${each.key}-sns-dlq"

  tags = merge(
    var.default_tags,
    each.value.tags,
    { "dlq-for" = each.key }
  )
}

# Policy to allow SNS -> SQS DLQ
resource "aws_sqs_queue_policy" "dlq_policy" {
  for_each = aws_sqs_queue.dlq

  queue_url = each.value.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = { Service = "sns.amazonaws.com" }
      Action = "sqs:SendMessage"
      Resource = each.value.arn
      Condition = {
        ArnEquals = {
          "aws:SourceArn" = aws_sns_topic.this[each.key].arn
        }
      }
    }]
  })
}

# SNS -> DLQ Redrive Policy
resource "aws_sns_topic_subscription" "dlq_subscription" {
  for_each = aws_sqs_queue.dlq

  topic_arn = aws_sns_topic.this[each.key].arn
  protocol  = "sqs"
  endpoint  = each.value.arn

  redrive_policy = jsonencode({
    deadLetterTargetArn = each.value.arn
  })
}

# Email Subscription
locals {
  email_subscriptions = flatten([
    for topic_name, cfg in var.topics : [
      for email in cfg.email_subscriptions : {
        topic_name = topic_name
        email      = email
      }
    ]
  ])
}

resource "aws_sns_topic_subscription" "email" {
  for_each = {
    for sub in local.email_subscriptions :
    "${sub.topic_name}:${sub.email}" => sub
  }

  topic_arn = aws_sns_topic.this[each.value.topic_name].arn
  protocol  = "email"
  endpoint  = each.value.email
}
