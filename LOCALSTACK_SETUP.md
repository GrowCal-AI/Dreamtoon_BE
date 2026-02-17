# LocalStack 로컬 S3 설정 가이드

## 🎯 LocalStack이란?

LocalStack은 AWS 서비스를 로컬에서 에뮬레이션하는 도구입니다. 실제 AWS 계정 없이 S3, DynamoDB, Lambda 등을 로컬에서 테스트할 수 있습니다.

---

## 🚀 사용 방법

### 1. LocalStack 시작

```bash
docker-compose up -d localstack
```

### 2. S3 버킷 확인

```bash
# LocalStack S3 버킷 목록 확인
docker exec dreamtoon-localstack awslocal s3 ls

# 출력 예시:
# 2026-02-17 12:00:00 dreamtoon-dev-bucket
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 Docker Compose로 전체 스택 실행:

```bash
docker-compose up
```

---

## 🔧 설정 파일

### `docker-compose.yml`
```yaml
localstack:
  image: localstack/localstack:latest
  ports:
    - "4566:4566"  # LocalStack gateway
  environment:
    - SERVICES=s3
```

### `application-local.yml`
```yaml
AWS_ACCESS_KEY_ID: test
AWS_SECRET_ACCESS_KEY: test
AWS_S3_BUCKET: dreamtoon-dev-bucket
AWS_S3_ENDPOINT: http://localhost:4566  # LocalStack
```

### `AwsS3Config.java`
```java
// LocalStack 엔드포인트 설정
if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
    s3ClientBuilder
        .endpointOverride(URI.create(s3Endpoint))
        .forcePathStyle(true);  // LocalStack은 path-style 필요
}
```

---

## 📝 수동 S3 작업

### 버킷 생성
```bash
docker exec dreamtoon-localstack awslocal s3 mb s3://my-bucket
```

### 파일 업로드
```bash
docker exec dreamtoon-localstack awslocal s3 cp test.txt s3://dreamtoon-dev-bucket/
```

### 파일 목록 확인
```bash
docker exec dreamtoon-localstack awslocal s3 ls s3://dreamtoon-dev-bucket/
```

### 파일 다운로드
```bash
docker exec dreamtoon-localstack awslocal s3 cp s3://dreamtoon-dev-bucket/test.txt ./
```

---

## 🧪 테스트

### E2E 테스트 실행
```bash
./test_e2e.sh
```

이제 웹툰 생성이 성공적으로 완료되어야 합니다!

---

## 🔄 프로덕션 환경

프로덕션에서는 실제 AWS S3를 사용합니다:

### `application-prod.yml`
```yaml
# AWS_S3_ENDPOINT를 설정하지 않으면 실제 AWS S3 사용
AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID}
AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY}
AWS_S3_BUCKET: dreamtoon-prod-bucket
AWS_REGION: ap-northeast-2
```

---

## 📚 참고 자료

- [LocalStack 공식 문서](https://docs.localstack.cloud/)
- [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
