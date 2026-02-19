# 🔧 DreamToon Backend 초기 세팅 가이드

## 📋 완료된 초기 세팅 항목

### ✅ 1. 프로젝트 기본 구조
- [x] Gradle 빌드 설정 (Java 17, Spring Boot 3.2.3)
- [x] Gradle Wrapper 설정
- [x] 패키지 구조 생성 (도메인 기반 패키징)
- [x] .gitignore 설정
- [x] README.md 작성

### ✅ 2. 의존성 설정
```gradle
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Spring Boot Starter Security
- Spring Boot Starter OAuth2 Client
- Spring AI (OpenAI)
- PostgreSQL Driver
- Querydsl
- Lombok

- Springdoc OpenAPI (Swagger)
- Hypersistence Utils (JSONB 지원)
```

### ✅ 3. 환경 설정 파일
- [x] `application.yml` - 공통 설정
- [x] `application-local.yml` - 로컬 개발 환경
- [x] `application-prod.yml` - 운영 환경
- [x] `.env.example` - 환경 변수 예시

### ✅ 4. Global 패키지
#### Common
- [x] `ApiResponse.java` - 공통 API 응답 DTO
- [x] `PageResponse.java` - 페이징 응답 DTO
- [x] `PageRequest.java` - 페이징 요청 DTO

#### Config
- [x] `SecurityConfig.java` - Spring Security 설정
- [x] `SwaggerConfig.java` - Swagger/OpenAPI 설정
- [x] `WebMvcConfig.java` - Web MVC 설정
- [x] `JpaConfig.java` - JPA 및 Querydsl 설정

#### Error
- [x] `ErrorCode.java` - 에러 코드 Enum
- [x] `BusinessException.java` - 비즈니스 예외
- [x] `EntityNotFoundException.java` - 엔티티 없음 예외
- [x] `GlobalExceptionHandler.java` - 전역 예외 핸들러

### ✅ 5. Infrastructure 패키지
#### Security
- [x] `JwtTokenProvider.java` - JWT 토큰 생성/검증
- [x] `JwtAuthenticationFilter.java` - JWT 인증 필터
- [x] `CustomOAuth2User.java` - OAuth2 사용자 객체
- [x] `CustomOAuth2UserService.java` - OAuth2 사용자 서비스
- [x] `OAuthAttributes.java` - OAuth2 속성 매핑
- [x] `OAuth2AuthenticationSuccessHandler.java` - OAuth2 성공 핸들러

### ✅ 6. Domain 패키지

#### User 도메인
- [x] `User.java` - 사용자 엔티티
- [x] `Role.java` - 권한 Enum
- [x] `SocialProvider.java` - 소셜 제공자 Enum
- [x] `UserRepository.java` - 사용자 레포지토리
- [x] `UserService.java` - 사용자 서비스
- [x] `UserController.java` - 사용자 컨트롤러
- [x] `UserResponse.java` - 사용자 응답 DTO
- [x] `TokenResponse.java` - 토큰 응답 DTO

#### Dream 도메인
- [x] `Dream.java` - 꿈 엔티티
- [x] `StylePreset.java` - 스타일 프리셋 Enum
- [x] `DreamRepository.java` - 꿈 레포지토리
- [x] `DreamService.java` - 꿈 서비스 (Mock 데이터 포함)
- [x] `DreamController.java` - 꿈 컨트롤러
- [x] `CreateDreamRequest.java` - 꿈 생성 요청 DTO
- [x] `DreamResponse.java` - 꿈 응답 DTO

#### Scene 도메인
- [x] `Scene.java` - 웹툰 장면 엔티티
- [x] `SceneRepository.java` - 장면 레포지토리
- [x] `SceneResponse.java` - 장면 응답 DTO

#### Analysis 도메인
- [x] `Analysis.java` - 건강 분석 엔티티 (JSONB 사용)
- [x] `AnalysisRepository.java` - 분석 레포지토리
- [x] `AnalysisResponse.java` - 분석 응답 DTO

### ✅ 7. 메인 애플리케이션
- [x] `DreamtoonBackendApplication.java` - Spring Boot 메인 클래스

---

## 🚀 다음 단계

### 1. 환경 설정
```bash
# .env 파일 생성 및 설정
cp .env.example .env

# .env 파일을 열어 다음 항목들을 설정하세요:
# - OPENAI_API_KEY
# - GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
# - KAKAO_CLIENT_ID, KAKAO_CLIENT_SECRET

# - JWT_SECRET (최소 256비트)
```

### 2. PostgreSQL 데이터베이스 설정
```bash
# Docker를 사용하는 경우
docker run --name dreamtoon-postgres \
  -e POSTGRES_DB=dreamtoon_dev \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15

# 또는 로컬 PostgreSQL 설치 후 데이터베이스 생성
createdb dreamtoon_dev
```

### 3. 빌드 및 실행
```bash
# 의존성 다운로드 및 빌드 (처음 한 번)
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 또는 JAR 파일로 실행
java -jar build/libs/dreamtoon-backend-0.0.1-SNAPSHOT.jar
```

### 4. API 테스트
브라우저에서 접속:
```
http://localhost:8080/swagger-ui.html
```

### 5. 구현해야 할 기능들

#### 우선순위 1: Spring AI 통합
- [ ] `DreamAiService.java` 생성
- [ ] GPT-4o를 통한 꿈 내용 장면 분할 (4-8컷)
- [ ] DALL-E 3를 통한 이미지 생성
- [ ] 감정 분석 및 DHI 점수 산출

#### 우선순위 2: GCP Storage 통합
- [ ] `GcsStorageService.java` 생성
- [ ] 이미지 업로드/다운로드 기능
- [ ] 이미지 URL 관리

#### 우선순위 3: 비동기 처리
- [ ] `@Async` 설정
- [ ] 꿈 생성 비동기 처리
- [ ] 진행 상태 조회 API

#### 우선순위 4: 테스트 코드
- [ ] Unit Tests (서비스 레이어)
- [ ] Integration Tests (API)
- [ ] Security Tests

---

## 📁 프로젝트 구조

```
dreamtoon-backend/
├── build.gradle                 # Gradle 빌드 설정
├── settings.gradle              # Gradle 프로젝트 설정
├── .gitignore                   # Git 제외 파일
├── .env.example                 # 환경 변수 예시
├── README.md                    # 프로젝트 설명
├── SETUP_GUIDE.md              # 이 파일
│
├── gradle/
│   └── wrapper/                 # Gradle Wrapper
│
└── src/
    ├── main/
    │   ├── java/com/dreamtoon/
    │   │   ├── DreamtoonBackendApplication.java
    │   │   │
    │   │   ├── domain/          # 도메인 패키지
    │   │   │   ├── dream/       # 꿈 도메인
    │   │   │   │   ├── controller/
    │   │   │   │   ├── dto/
    │   │   │   │   ├── entity/
    │   │   │   │   ├── repository/
    │   │   │   │   └── service/
    │   │   │   ├── user/        # 사용자 도메인
    │   │   │   ├── scene/       # 장면 도메인
    │   │   │   └── analysis/    # 분석 도메인
    │   │   │
    │   │   ├── global/          # 전역 설정
    │   │   │   ├── common/      # 공통 DTO, 애노테이션
    │   │   │   ├── config/      # Spring 설정
    │   │   │   └── error/       # 예외 처리
    │   │   │
    │   │   └── infrastructure/  # 인프라 레이어
    │   │       ├── security/    # 보안
    │   │       ├── persistence/ # 영속성
    │   │       └── storage/     # 스토리지 (예정)
    │   │
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.yml
    │       └── application-prod.yml
    │
    └── test/
        └── java/com/dreamtoon/  # 테스트 코드 (예정)
```

---

## 🔑 주요 API 엔드포인트

### OAuth2 로그인
- GET `/oauth2/authorization/google` - Google 로그인
- GET `/oauth2/authorization/kakao` - Kakao 로그인

### Users
- GET `/api/v1/users/me` - 내 정보 조회
- PATCH `/api/v1/users/me/nickname` - 닉네임 변경

### Dreams
- POST `/api/v1/dreams` - 꿈 생성
- GET `/api/v1/dreams/{id}` - 꿈 상세 조회
- GET `/api/v1/dreams` - 내 꿈 목록
- DELETE `/api/v1/dreams/{id}` - 꿈 삭제

---

## 💡 주요 기술 결정 사항

### 1. 도메인 기반 패키징
- 기능별 응집도를 높이고 유지보수성 향상
- 각 도메인이 독립적으로 관리 가능

### 2. Spring AI 사용
- Python 서버 없이 Spring Boot에서 직접 OpenAI API 호출
- 빠른 MVP 개발 가능

### 3. PostgreSQL + JSONB
- 정형 데이터(User, Dream)는 일반 컬럼
- 비정형 데이터(감정 분석 결과)는 JSONB로 유연하게 저장

### 4. JWT + OAuth2
- Stateless 인증
- 소셜 로그인 지원

### 5. Querydsl
- 타입 세이프한 쿼리 작성
- 복잡한 동적 쿼리 처리

---

## 🐛 트러블슈팅

### Gradle Build 실패
```bash
# 1. Gradle Wrapper 재생성
./gradlew wrapper --gradle-version 8.5

# 2. 캐시 삭제 후 재빌드
./gradlew clean build --refresh-dependencies
```

### PostgreSQL 연결 실패
```bash
# PostgreSQL 실행 확인
docker ps | grep postgres

# 로그 확인
docker logs dreamtoon-postgres
```

### JWT Secret 길이 오류
```
# .env 파일의 JWT_SECRET을 최소 256비트로 설정
# 예시: openssl rand -base64 32
```

---

## 📚 참고 자료

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)
- [PostgreSQL JSONB](https://www.postgresql.org/docs/current/datatype-json.html)

---

**마지막 업데이트**: 2026-02-13
**설정 완료**: ✅ 초기 세팅 완료
