# 🚀 PROJECT_BLUEPRINT: DreamToon (드림툰)

> **AI 기반 꿈 시각화 및 정서 상태 분석 헬스케어 플랫폼**
> 본 문서는 해커톤 MVP 개발을 위한 기술 설계 및 기획 가이드라인입니다.

---

## 1. 프로젝트 개요 (Overview)
- **핵심 가치**: 휘발되는 꿈 데이터를 시각적 콘텐츠(웹툰)로 변환하고, 무의식 속 건강 지표를 도출함.
- **주요 타겟**: 기록의 재미를 느끼고 싶은 사용자, 심리적 불안정감을 해소하고 싶은 현대인.
- **차별점**: 
    - LLM 기반 자동 장면 분할 및 시나리오 생성.
    - Stable Diffusion(DALL-E) 기반 웹툰 스타일 프리셋 적용.
    - '드림 헬스 인덱스(DHI)'를 통한 정서 상태 정량화.

---

## 2. 기술 스택 (Technical Stack)

| Category | Technology | Reason |
| :--- | :--- | :--- |
| **Backend** | **Spring Boot 3.2+** | 익숙한 생태계, 안정적인 아키텍처 및 확장성 확보 |
| **AI Library** | **Spring AI (OpenAI)** | 파이썬 서버 분리 없이 빠른 MVP 구현 가능 |
| **Database** | **PostgreSQL** | JSONB 지원으로 비정형 분석 데이터 및 시나리오 저장 용이 |
| **Storage** | **AWS S3** | 생성된 웹툰 이미지 호스팅 |
| **Frontend** | **Next.js** | 세로 스크롤 웹툰 뷰어 및 대시보드 구현 최적화 |

---

## 3. 핵심 아키텍처 (System Flow)



1. **Input**: 사용자가 텍스트/음성으로 꿈을 기록함.
2. **Processing (Spring AI)**:
   - **Text-to-Scene**: GPT-4o가 꿈을 4~8컷 시나리오로 분할.
   - **Text-to-Analysis**: 꿈의 키워드와 감정을 추출하여 DHI 점수 산출.
   - **Scene-to-Image**: DALL-E를 호출하여 스타일 프리셋이 적용된 이미지 생성.
3. **Storage**: 분석 결과와 이미지 경로를 PostgreSQL(JSONB)에 저장.
4. **Output**: 사용자에게 웹툰 뷰어와 건강 대시보드 제공.

---

## 4. 데이터 모델링 (Entity Design)

### 👤 User Entity (Member)
- `id` (Long, PK)
- `email` (String, Unique): 로그인 및 본인 식별용 계정
- `nickname` (String): 서비스 내에서 사용될 이름
- `social_provider` (Enum): GOOGLE, KAKAO 등 (OAuth2 연동 대비)
- `social_id` (String): 소셜 서비스에서 제공하는 고유 식별값
- `role` (Enum): ROLE_USER, ROLE_ADMIN (권한 관리)

created_at (DateTime): 가입일

### 📊 Dream Entity
- `id` (Long, PK)
- `user_id` (Long, FK)
- `raw_content` (Text): 사용자 입력 원문
- `style_preset` (Enum): ROMANCE, FANTASY, HEALING, SD_REFRAME
- `created_at` (DateTime)

### 🎨 Scene Entity (Webtoon Cuts)
- `id` (Long, PK)
- `dream_id` (Long, FK)
- `cut_order` (Integer): 장면 순서 (1~N)
- `description` (String): AI가 생성한 장면 묘사
- `image_url` (String): S3 이미지 경로
- `dialogue` (String): 필요 시 삽입될 대사/설명

### 🩺 Analysis Entity (Healthcare)
- `id` (Long, PK)
- `dream_id` (Long, FK)
- `health_score` (Integer): 0~100 점수
- `emotions` (Jsonb): {joy: 0.1, fear: 0.7, ...}
- `ai_insight` (String): AI가 주는 일일 코칭 메시지

---

## 5. 주요 API 명세 (API Specification)

### [POST] /api/v1/dreams
- **Description**: 꿈 기록 제출 및 AI 생성 시작.
- **Request Body**:
  ```json
  {
    "content": "어젯밤 숲속에서 거대한 고양이와 산책을 했어.",
    "style": "HEALING"
  }
  ```
- Response: `202 Accepted` (이미지 생성 시간이 길어질 수 있으므로 비동기 처리 권장)

### [GET] /api/v1/dreams/{id}
- Description: 생성된 웹툰 및 분석 데이터 상세 조회.

- Response:
  ```json
  {
    "dream_id": 1,
    "scenes": [...],
    "analysis": { "health_score": 85, "insight": "편안한 꿈을 꾸셨네요!" }
  }
  ```