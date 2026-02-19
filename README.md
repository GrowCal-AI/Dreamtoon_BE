# 🌙 DreamToon Backend

> AI 기반 꿈 시각화 및 정서 상태 분석 헬스케어 플랫폼

꿈을 웹툰으로 변환하고, 무의식 속 건강 지표(드림 헬스 인덱스)를 제공하는 Spring Boot 백엔드 서비스입니다.

## 📚 기술 스택

- **Framework**: Spring Boot 3.2.3
- **Language**: Java 17
- **Database**: PostgreSQL (JSONB 지원)
- **AI**: Spring AI (OpenAI GPT-4o, DALL-E 3)
- **Security**: Spring Security + OAuth2 (Google, Kakao)
- **Storage**: GCP Google Cloud Storage
- **ORM**: Spring Data JPA + Querydsl
- **Build Tool**: Gradle (Groovy)

## 🏗️ 프로젝트 구조

```
src/main/java/com/dreamtoon/
├── domain/                      # 도메인 기반 패키징
│   ├── dream/                   # 꿈 기록 도메인
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   ├── user/                    # 사용자 도메인
│   ├── scene/                   # 웹툰 장면 도메인
│   └── analysis/                # 건강 분석 도메인
├── global/                      # 전역 설정
│   ├── common/                  # 공통 DTO, 애노테이션
│   ├── config/                  # Spring 설정
│   └── error/                   # 예외 처리
└── infrastructure/              # 인프라스트럭처
    ├── security/                # 인증/인가
    ├── persistence/             # 영속성
    └── storage/                 # 파일 저장소
```

## 🚀 빠른 시작 (Quick Start)

### 방법 1: Docker Compose 사용 (추천! 🎯)

**팀원이라면 이것만 하면 끝!**

```bash
# 1. 저장소 클론
git clone <repository-url>
cd Dreamtoon_BE

# 2. 환경 변수 설정 (선택사항)
cp .env.example .env
# .env 파일에서 필요한 API 키 설정

# 3. Docker Compose로 실행 (PostgreSQL + Spring Boot)
docker-compose up

# 완료! 🎉
# http://localhost:8080 접속 가능
# http://localhost:8080/swagger-ui.html 에서 API 문서 확인
```

**중지:**
```bash
docker-compose down
```

**재시작 (코드 변경 시):**
```bash
docker-compose up --build
```

**백그라운드 실행:**
```bash
docker-compose up -d
```

---

### 방법 2: 로컬 개발 환경 (개발자용)

개발 중 빠른 피드백이 필요한 경우 (IntelliJ 디버거, 즉시 리로드 등)

#### 1. 환경 변수 설정

```bash
cp .env.example .env
```

`.env` 파일을 열어 필요한 값들을 설정합니다:
- OpenAI API Key
- OAuth2 Client ID/Secret (Google, Kakao)
- JWT Secret

#### 2. PostgreSQL 실행 (Docker)

```bash
docker run --name dreamtoon-postgres \
  -e POSTGRES_DB=dreamtoon_dev \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

#### 3. Spring Boot 실행

```bash
# Unix/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

## 📖 API 문서

애플리케이션 실행 후 Swagger UI를 통해 API 문서를 확인할 수 있습니다:

```
http://localhost:8080/swagger-ui.html
```

## 🔑 주요 API 엔드포인트

### Dreams (꿈 기록)

- `POST /api/v1/dreams` - 꿈 기록 생성 및 웹툰 생성 시작
- `GET /api/v1/dreams/{id}` - 꿈 상세 조회 (웹툰 + 분석)
- `GET /api/v1/dreams` - 내 꿈 목록 조회
- `DELETE /api/v1/dreams/{id}` - 꿈 삭제

### Users (사용자)

- `GET /api/v1/users/me` - 내 정보 조회
- `PATCH /api/v1/users/me/nickname` - 닉네임 변경

### OAuth2 Login

- Google: `/oauth2/authorization/google`
- Kakao: `/oauth2/authorization/kakao`

## 🔐 인증 방식

1. OAuth2 소셜 로그인 (Google, Kakao)
2. JWT 토큰 기반 인증
3. Access Token (1시간) + Refresh Token (7일)

헤더에 토큰 포함:
```
Authorization: Bearer {access_token}
```

## 🌍 환경별 프로파일

- `local`: 로컬 개발 환경 (application-local.yml)
- `prod`: 운영 환경 (application-prod.yml)

환경 전환:
```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

## 📦 빌드

```bash
# 테스트 포함 빌드
./gradlew build

# 테스트 제외 빌드
./gradlew build -x test

# JAR 파일 위치
build/libs/dreamtoon-backend-0.0.1-SNAPSHOT.jar
```

## 🧪 테스트

```bash
./gradlew test
```

## 📝 데이터베이스 스키마

주요 엔티티:
- **User**: 사용자 정보 (OAuth2 연동)
- **Dream**: 꿈 기록
- **Scene**: 웹툰 장면 (Dream의 컷)
- **Analysis**: 건강 분석 데이터 (JSONB로 감정 데이터 저장)

## 🔧 개발 도구

- **Querydsl**: 타입 세이프한 쿼리 작성
- **Lombok**: 보일러플레이트 코드 제거
- **Spring AI**: OpenAI API 통합
- **Hypersistence Utils**: PostgreSQL JSONB 지원

---

## 🔐 환경 변수 설정

### 필수 환경 변수

`.env` 파일을 생성하고 다음 변수들을 설정하세요:

```bash
# OpenAI API (필수)
OPENAI_API_KEY=sk-proj-...

# OAuth2 - Google (선택사항)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# OAuth2 - Kakao (선택사항)
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret

# JWT (필수)
JWT_SECRET=your-jwt-secret-key-must-be-at-least-256-bits-long
```

### 환경 변수 우선순위

1. 시스템 환경 변수
2. `.env` 파일
3. `docker-compose.yml`의 기본값
4. `application.yml`의 기본값

---

## 🐛 트러블슈팅

### Docker Compose 실행 시 포트 충돌

```bash
# 에러: port is already allocated
# 해결: 실행 중인 PostgreSQL 중지
docker stop dreamtoon-postgres

# 또는 다른 포트 사용 (docker-compose.yml 수정)
ports:
  - "5433:5432"  # 로컬 5433 포트 사용
```

### 빌드 실패 (Querydsl Q클래스 없음)

```bash
# 해결: Querydsl 클래스 재생성
./gradlew clean compileJava
docker-compose up --build
```

### 환경 변수 인식 안 됨

```bash
# .env 파일 위치 확인 (프로젝트 루트에 있어야 함)
ls -la .env

# docker-compose.yml에서 env_file 추가 (필요 시)
services:
  backend:
    env_file:
      - .env
```

### 데이터베이스 초기화

```bash
# 모든 데이터 삭제 후 재시작
docker-compose down -v  # 볼륨까지 삭제
docker-compose up
```

---

## 📌 TODO

- [ ] Spring AI를 통한 실제 GPT-4o 장면 분할 구현
- [ ] DALL-E 3 이미지 생성 비동기 처리
- [ ] 감정 분석 및 DHI 점수 산출 로직 구현
- [ ] GCP Storage 이미지 업로드 구현
- [ ] 테스트 코드 작성
- [ ] CI/CD 파이프라인 구축

## 📄 라이센스

This project is licensed under the MIT License.

## 👥 Contributors

- Backend Developer: [Your Name]

---

**Made with ❤️ for Hackathon MVP**
