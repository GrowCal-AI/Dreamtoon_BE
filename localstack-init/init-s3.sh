#!/bin/bash

# LocalStack 초기화 스크립트
# S3 버킷 생성

echo "Creating S3 bucket: dreamtoon-dev-bucket"
awslocal s3 mb s3://dreamtoon-dev-bucket

echo "S3 bucket created successfully!"
awslocal s3 ls
