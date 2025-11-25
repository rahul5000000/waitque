output "cdn_domain" {
  value = aws_cloudfront_distribution.cdn.domain_name
}

output "s3_bucket" {
  value = aws_s3_bucket.bucket.id
}