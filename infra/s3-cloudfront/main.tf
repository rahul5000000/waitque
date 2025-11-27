############################
# S3 Bucket
############################
resource "aws_s3_bucket" "bucket" {
  bucket = var.bucket_name
  force_destroy = var.force_destroy
}

resource "aws_s3_bucket_ownership_controls" "ownership" {
  bucket = aws_s3_bucket.bucket.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_public_access_block" "block" {
  bucket                  = aws_s3_bucket.bucket.id
  block_public_acls       = true
  block_public_policy     = false
  ignore_public_acls      = true
  restrict_public_buckets = false
}

resource "aws_s3_bucket_versioning" "versioning" {
  bucket = aws_s3_bucket.bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "sse" {
  bucket = aws_s3_bucket.bucket.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Lifecycle: expire old versions
resource "aws_s3_bucket_lifecycle_configuration" "lifecycle" {
  bucket = aws_s3_bucket.bucket.id

  rule {
    id     = "expire-versions"
    status = "Enabled"

    noncurrent_version_expiration {
      noncurrent_days = var.expire_noncurrent_days
    }
  }
}

###############################################
# CloudFront OAC
###############################################
resource "aws_cloudfront_origin_access_control" "oac" {
  name                              = "${var.bucket_name}-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

###############################################
# CloudFront Distribution
###############################################
resource "aws_cloudfront_distribution" "cdn" {
  enabled             = true
  default_root_object = var.default_root_object
  price_class         = "PriceClass_100"

  origin {
    domain_name              = aws_s3_bucket.bucket.bucket_regional_domain_name
    origin_id                = "s3origin"
    origin_access_control_id = aws_cloudfront_origin_access_control.oac.id
  }

  default_cache_behavior {
    target_origin_id       = "s3origin"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }
  }

  restrictions {
    geo_restriction {
      restriction_type = "whitelist"
      locations        = var.geo_whitelist
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }
}

###############################################
# S3 Bucket Policy
###############################################
resource "aws_s3_bucket_policy" "bucket_policy" {
  bucket = aws_s3_bucket.bucket.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [

      # CloudFront OAC GET Access
      {
        Sid      = "AllowCloudFrontRead",
        Effect   = "Allow",
        Principal = {
          Service = "cloudfront.amazonaws.com"
        },
        Action   = ["s3:GetObject"],
        Resource = "${aws_s3_bucket.bucket.arn}/*",
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.cdn.arn
          }
        }
      },

      # Backend IAM Role PUT access
      {
        Sid    = "AllowBackendUploads",
        Effect = "Allow",
        Principal = {
          AWS = var.backend_role_arn
        },
        Action   = ["s3:PutObject", "s3:PutObjectAcl"],
        Resource = "${aws_s3_bucket.bucket.arn}/*"
      }
    ]
  })
}

resource "aws_s3_bucket_cors_configuration" "cors" {
  bucket = aws_s3_bucket.bucket.id

  cors_rule {
    id = "allow-presigned-uploads"

    allowed_methods = ["PUT", "GET"]
    allowed_origins = ["*"]        # you can restrict later
    allowed_headers = ["*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

###############################################
# Outputs
###############################################
output "bucket_name" {
  value = aws_s3_bucket.bucket.bucket
}

output "bucket_arn" {
  value = aws_s3_bucket.bucket.arn
}

output "cloudfront_domain" {
  value = aws_cloudfront_distribution.cdn.domain_name
}
