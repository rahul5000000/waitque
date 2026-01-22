output "topic_arns" {
  value = {
    for name, topic in aws_sns_topic.this :
    name => topic.arn
  }
}

output "dlq_arns" {
  value = {
    for name, queue in aws_sqs_queue.dlq :
    name => queue.arn
  }
}

output "email_subscriptions" {
  value = {
    for k, sub in aws_sns_topic_subscription.email :
    k => sub.endpoint
  }
}
