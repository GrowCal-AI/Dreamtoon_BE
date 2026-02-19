# 🚀 PROJECT_BLUEPRINT: DreamToon (드림툰)

> **AI 기반 꿈 시각화 및 정서 상태 분석 헬스케어 플랫폼**
> 본 문서는 해커톤 MVP 개발을 위한 기술 설계 및 기획 가이드라인입니다.

---

## 1. 프로젝트 개요 (Overview)

- **핵심 가치**: 휘발되는 꿈 데이터를 시각적 콘텐츠(4컷 만화)로 변환하고, 무의식 속 건강 지표를 도출함.
- **주요 타겟**: 기록의 재미를 느끼고 싶은 사용자, 심리적 불안정감을 해소하고 싶은 현대인.
- **차별점**:
    - 단계별 인터랙티브 꿈 입력 플로우 (꿈 내용 → 감정 선택 → 상세 설명 → 필터 선택)
    - GPT-4o 기반 꿈 분석 및 감정 레이더 차트 생성
    - DALL-E / GPT-Image-1 기반 장르별 4컷 만화 자동 생성
    - 심리상담사 페르소나 AI 챗봇을 통한 꿈 심층 상담
    - 개인 라이브러리를 통한 꿈 아카이빙 및 패턴 분석
    - **4단계 구독 시스템 (FREE / PLUS / PRO / ULTRA)**
    - **Polar.sh 기반 구독 결제 연동**

---

## 2. 기술 스택 (Technical Stack)

| Category | Technology | Reason |
| :--- | :--- | :--- |
| **Backend** | **Spring Boot 3.2+** | 익숙한 생태계, 안정적인 아키텍처 및 확장성 확보 |
| **AI Library** | **Spring AI (OpenAI)** | GPT-4o, DALL-E / GPT-Image-1 통합 API 호출 간소화 |
| **Database** | **PostgreSQL** | JSONB 지원으로 감정 분석 데이터 및 채팅 내역 저장 용이 |
| **Storage** | **GCS (Google Cloud Storage)** | 생성된 4컷 만화 이미지 호스팅 |
| **Async Processing** | **Spring @Async** | 비동기 AI API 호출로 사용자 대기 시간 최소화 |
| **Payment** | **Polar.sh** | 구독 결제 + Webhook 기반 티어 동기화 |
| **Frontend** | **Next.js** | 인터랙티브 UI 및 웹툰 뷰어 구현 최적화 |

---

## 3. 핵심 사용자 플로우 (User Flow)

### 📱 **Phase 1: 꿈 입력 및 감정 선택**

```
[메인 화면]
  ↓ 사용자 입력: "나 어제 썸녀와 데이트하는 꿈 꿨어"

[감정 선택 화면]
  시스템 메시지: "안녕하세요! 어젯밤 꾸셨던 꿈은 어떠셨나요?"
  ↓ 사용자 선택: 😊 기쁨 / 😢 불안 / 😤 분노 / 😰 슬픔 / 😲 놀라움 / 😑 평온

[상세 설명 입력]
  시스템 메시지 (감정별 매핑): "좋은 꿈을 꾸셨군요! 어떤 점이 가장 즐거우셨나요?"
  ↓ 사용자 입력: "카페에서 데이트했는데 분위기가 너무 좋았어요"
  ↓ (선택) 현실 상황 고민: "요즘 그 사람한테 고백할까 고민중이에요"
```

### 🤖 **Phase 2: AI 꿈 분석 (비동기)**

```
[백엔드 처리]
  → GPT-4o API 호출
  → 입력 데이터:
     - 꿈 내용
     - 선택한 감정
     - 상세 설명
     - 현실 고민 (optional)

  → 출력 데이터:
     - 꿈 해석 텍스트
     - 감정 레이더 차트 점수 (기쁨, 불안, 분노, 슬픔, 놀라움, 평온)
     - AI 인사이트 메시지
```

### 🎨 **Phase 3: 필터 선택 및 4컷 만화 생성**

```
[필터 선택 화면]
  ─ 스탠다드 필터 ─
  맞춤형 (CUSTOM): AI가 꿈 내용에 맞게 자동 선택

  ─ 프리미엄 필터 (PRO 뱃지) ─
  지브리 / 마블 / 레고 / 모동숲

  ※ 스탠다드 이미지 쿼터 = 맞춤형 필터 사용
  ※ 프리미엄 이미지 쿼터 = 프리미엄 필터 사용

[백엔드 처리]
  1단계: GPT로 4컷 스토리보드 생성 (기승전결 + Character DNA)
  2단계: 각 패널을 이미지 생성 프로바이더로 병렬 생성 (재시도 포함)
  → GCS에 업로드 후 URL 저장
```

### 💬 **Phase 4: 심리상담 챗봇 (선택)**

```
[꿈 더 대화하기 클릭]
  → 심리상담사 페르소나 GPT 챗봇 시작
  → 해당 꿈에 대한 심층 상담
  → 채팅 내역 DB 저장
  ※ 무료 회원: 꿈당 3턴 제한
```

### 📚 **Phase 5: 라이브러리 관리**

```
[라이브러리 화면]
  - 검색: 제목/내용으로 검색
  - 필터링:
    ✅ 즐겨찾기
    ✅ 최신순 정렬
    ✅ 장르별 필터

  - 각 꿈 카드:
    - 썸네일 (4컷 중 첫 번째 이미지)
    - 제목
    - 날짜
    - 장르 태그
    - 즐겨찾기 토글
```

---

## 4. 구독 시스템 (Subscription System)

### 📊 **4단계 구독 티어**

| 구분 | 무료 (FREE) | Plus (₩1,990/월) | Pro (₩9,900/월) | Ultra (₩19,900/월) |
|------|------------|-----------------|-----------------|-------------------|
| **스탠다드 이미지** | 월 1회 | 월 5회 | 월 20회 | 무제한 |
| **프리미엄 이미지** | 최초 1회* | 월 1회 | 월 5회 | 월 20회 |
| **감정 분석** | 월 1회 | 월 5회 | 무제한 | 무제한 |
| **꿈 내용 상담** | 월 1회 | 월 5회 | 월 30회 | 무제한 |
| **이미지 저장** | 총 10개 | 총 20개 | 무제한 | 무제한 |
| **건강 측정** | 월 1회 | 월 1회 | 매주 | AI 기반 매일 |

> *프리미엄 이미지 최초 1회: 회원가입 후 로그인 시 1회 제공, 영구 소진 (premiumTrialUsed 플래그)

### 🎨 **필터 분류**

| 필터 | 코드 | 구분 | 설명 |
|------|------|------|------|
| 맞춤형 | CUSTOM | 스탠다드 | AI가 꿈 내용에 맞게 자동 선택 |
| 지브리 | GHIBLI | 프리미엄 | studio ghibli style, hand-drawn watercolor |
| 마블 | MARVEL | 프리미엄 | american superhero comic style, bold ink lines |
| 레고 | LEGO | 프리미엄 | toy brick figure style, blocky plastic characters |
| 모동숲 | ANIMAL_CROSSING | 프리미엄 | cute chibi village life style, pastel colors |

> **IP 안전 처리**: 프론트에서는 브랜드명 표시, 백엔드 DALL-E 프롬프트에서는 브랜드명 없이 스타일 묘사만 사용

### 🔑 **PRO 뱃지 의미**
- 프리미엄 이미지 쿼터를 소비하는 필터 (브랜드명 ≠ 티어명)
- **Plus 이상** 모두 사용 가능 (Plus: 월 1회, Pro: 월 5회, Ultra: 월 20회)
- 무료 회원: `premiumTrialUsed = false`인 경우에만 1회 사용 가능

### 👤 **비회원 (Guest) 처리**
- 별도 비회원 전용 API 엔드포인트 제공
- 스탠다드 이미지 월 1회 체험 (IP 기반 또는 세션 기반)
- 프리미엄 필터 사용 불가
- 라이브러리/즐겨찾기/채팅 불가

---

## 5. 결제 시스템 (Payment - Polar.sh)

### 🔌 **Polar.sh 연동 방식**

```
[결제 플로우]
프론트 → POST /api/v1/subscriptions/checkout?tier=PRO
백엔드 → Polar.sh API로 Checkout URL 생성
백엔드 → { "checkoutUrl": "https://polar.sh/checkout/..." } 반환
프론트 → 사용자를 Polar 결제 페이지로 리다이렉트

[결제 완료 후]
Polar.sh → POST /api/v1/webhooks/polar (Webhook)
백엔드 → Webhook 시그니처 검증
백엔드 → subscription.tier 업데이트 (FREE → PRO 등)
```

### 📋 **Polar Webhook 이벤트**

| 이벤트 | 처리 내용 |
|--------|----------|
| `subscription.created` | 구독 시작 → 해당 티어로 업그레이드 |
| `subscription.updated` | 티어 변경 → 새 티어로 업데이트 |
| `subscription.active` | 구독 활성화 확인 |
| `subscription.canceled` | 구독 취소 → 기간 만료 시 FREE 다운그레이드 |
| `subscription.revoked` | 즉시 FREE 다운그레이드 |

### 🧪 **개발자 테스트 환경**
- Polar.sh 샌드박스 모드 사용 (`POLAR_SANDBOX=true`)
- `application-local.yml`에서 기본 티어를 ULTRA로 설정하여 쿼터 없이 테스트
- 관리자 API: `PATCH /api/v1/admin/users/{userId}/subscription` (개발/테스트용 티어 강제 변경)

---

## 6. 시스템 아키텍처 (System Architecture)

### 🔄 **비동기 처리 전략**

```
사용자 요청 → 즉시 202 Accepted 응답
              ↓
         비동기 작업 시작
              ↓
    ┌─────────┴─────────┐
    │                   │
 GPT-4o 분석      이미지 생성 (4패널 병렬)
    │              GPT 스토리보드 → DALL-E/GPT-Image-1
    └─────────┬─────────┘
              ↓
         GCS 업로드
              ↓
    status: COMPLETED
```

### 🖼️ **이미지 생성 전략**

```
현재: 4패널 × 개별 API 호출 = 4회 (고품질, 고비용)
대안: 4컷 단일 프롬프트 → 1회 API 호출 (75% 비용 절감, 품질 타협)

기본값: gpt-image-1 (quality=medium)
개발환경: quality=low (비용 절감)
```

### 💾 **데이터 저장 전략**

- **시스템 메시지**: 하드코딩 (감정별 매핑 Map)
- **사용자 입력**: 모두 DB 저장 (꿈 내용, 감정, 상세 설명, 현실 고민)
- **AI 생성 데이터**: PostgreSQL JSONB 활용
- **이미지**: GCS 저장 후 URL만 DB에 저장

---

## 7. 데이터 모델링 (Entity Design)

### 👤 **User Entity**
```java
- id (Long, PK)
- email (String, Unique)
- nickname (String)
- social_provider (Enum): GOOGLE, KAKAO
- social_id (String)
- role (Enum): ROLE_USER, ROLE_ADMIN
- created_at (DateTime)
```

### 📊 **Dream Entity**
```java
- id (Long, PK)
- user_id (Long, FK)

// 사용자 입력 데이터
- dream_content (Text): 꿈 내용 원문
- primary_emotion (Enum): JOY, ANXIETY, ANGER, SADNESS, SURPRISE, PEACE
- detailed_description (Text): 상세 설명
- real_life_context (Text, nullable): 현실 고민

// AI 생성 데이터
- title (String): AI가 생성한 제목
- ai_analysis (Text): 꿈 해석
- emotion_scores (Jsonb): {"기쁨": 85, "불안": 20, "놀라움": 10, ...}
- ai_insight (String): AI 코칭 메시지

// 웹툰 관련
- selected_genre (Enum): CUSTOM, GHIBLI, MARVEL, LEGO, ANIMAL_CROSSING, ...
- webtoon_images (Jsonb): ["gcs://url1", "gcs://url2", ...]

// 라이브러리 관련
- is_favorite (Boolean): 즐겨찾기 여부
- is_in_library (Boolean): 라이브러리 등록 여부

// 처리 상태
- processing_status (Enum): PENDING, ANALYZING, ANALYSIS_COMPLETED, GENERATING, COMPLETED, FAILED

- created_at (DateTime)
- updated_at (DateTime)
```

### 💳 **Subscription Entity**
```java
- id (Long, PK)
- user_id (Long, FK, Unique)
- tier (Enum): FREE, PLUS, PRO, ULTRA
- is_active (Boolean)

// 스탠다드 이미지 쿼터
- standard_generation_count (Integer): 현재 월 스탠다드 이미지 사용 횟수

// 프리미엄 이미지 쿼터
- premium_generation_count (Integer): 현재 월 프리미엄 이미지 사용 횟수
- premium_trial_used (Boolean): 최초 1회 무료 프리미엄 사용 여부 (영구)

// 기타 쿼터
- library_count (Integer)
- favorite_count (Integer)
- chat_standard_count (Integer): 현재 월 감정 분석 사용 횟수

// 결제 정보 (Polar.sh)
- polar_subscription_id (String, nullable): Polar 구독 ID
- polar_customer_id (String, nullable): Polar 고객 ID
- subscription_end_date (LocalDate, nullable): 구독 만료일

- quota_reset_date (LocalDate): 다음 쿼터 리셋일 (매월 1일)
- created_at (DateTime)
```

### 💬 **DreamChat Entity**
```java
- id (Long, PK)
- dream_id (Long, FK)
- role (Enum): USER, ASSISTANT
- message (Text)
- created_at (DateTime)
```

---

## 8. 주요 API 명세 (API Specification)

### **꿈 플로우 API** (기존 유지)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/dreams` | 꿈 생성 시작 |
| PATCH | `/api/v1/dreams/{id}/emotion` | 감정 선택 |
| PATCH | `/api/v1/dreams/{id}/details` | 상세 설명 + 분석 시작 |
| GET | `/api/v1/dreams/{id}/analysis` | 분석 결과 폴링 |
| POST | `/api/v1/dreams/{id}/webtoon` | 필터 선택 + 웹툰 생성 |
| GET | `/api/v1/dreams/{id}` | 완성된 꿈 조회 |
| DELETE | `/api/v1/dreams/{id}` | 꿈 삭제 |
| POST | `/api/v1/dreams/{id}/library` | 라이브러리 등록 |
| PATCH | `/api/v1/dreams/{id}/favorite` | 즐겨찾기 토글 |
| POST | `/api/v1/dreams/{id}/chat` | 챗봇 메시지 전송 |
| GET | `/api/v1/dreams/{id}/chat` | 채팅 내역 조회 |

### **비회원 전용 API** (신규)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/guest/dreams` | 비회원 꿈 생성 (1회 체험) |
| PATCH | `/api/v1/guest/dreams/{id}/emotion` | 비회원 감정 선택 |
| PATCH | `/api/v1/guest/dreams/{id}/details` | 비회원 상세 설명 + 분석 |
| GET | `/api/v1/guest/dreams/{id}/analysis` | 비회원 분석 결과 |
| POST | `/api/v1/guest/dreams/{id}/webtoon` | 비회원 웹툰 생성 (스탠다드만) |
| GET | `/api/v1/guest/dreams/{id}` | 비회원 결과 조회 |

### **구독/결제 API** (신규)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/subscriptions/usage` | 현재 쿼터 사용량 조회 |
| POST | `/api/v1/subscriptions/checkout` | Polar Checkout URL 생성 |
| GET | `/api/v1/subscriptions/portal` | Polar 고객 포털 URL 반환 |
| POST | `/api/v1/webhooks/polar` | Polar Webhook 수신 (공개) |

### **관리자 API** (신규 - 개발/테스트용)

| Method | Path | 설명 |
|--------|------|------|
| PATCH | `/api/v1/admin/users/{userId}/subscription` | 티어 강제 변경 |

### **권한 정보 API**

```json
// GET /api/v1/users/me/permissions
{
  "tier": "FREE",
  "quota": {
    "standardImages": { "used": 1, "limit": 1, "remaining": 0 },
    "premiumImages": { "used": 0, "limit": 1, "remaining": 1, "isTrial": true },
    "analysis": { "used": 0, "limit": 1, "remaining": 1 },
    "chat": { "used": 0, "limit": 1, "remaining": 1 },
    "library": { "used": 5, "limit": 10, "remaining": 5 },
    "resetDate": "2026-03-01"
  },
  "permissions": {
    "canUseStandardFilter": true,
    "canUsePremiumFilter": true,
    "availableFilters": ["CUSTOM", "GHIBLI"]
  }
}
```

---

## 9. AI 프롬프트 전략 (Prompt Engineering)

### 🧠 **꿈 분석 프롬프트 (GPT-4o)**

```json
// 출력 JSON 키 — EmotionType enum description과 정확히 일치해야 함
{
  "title": "꿈 제목 (10자 이내)",
  "analysis": "꿈 해석 (200자 이내)",
  "emotionScores": {
    "기쁨": 0,
    "불안": 0,
    "분노": 0,
    "슬픔": 0,
    "놀라움": 0,   ← SURPRISE (不 놀람)
    "평온": 0
  },
  "insight": "AI 코칭 메시지 (100자 이내)"
}
```

### 🎨 **4컷 스토리보드 → 이미지 생성 (2단계)**

```
1단계: GPT → { characterDNA, scenes[4] } JSON 생성
       (Character DNA로 4컷 캐릭터 일관성 보장)

2단계: 각 scene + characterDNA → 이미지 생성 프로바이더 (병렬)
       - 재시도 시 softenPromptForRetry() 적용 (content policy 회피)
       - 브랜드명 없이 스타일 묘사만 사용

[프리미엄 필터 promptTemplate]
- GHIBLI: "studio ghibli style, hand-drawn watercolor, soft nature, whimsical atmosphere"
- MARVEL: "american superhero comic style, bold ink lines, halftone dots, dynamic action poses"
- LEGO: "toy brick figure style, blocky plastic characters, bright primary colors, plastic texture"
- ANIMAL_CROSSING: "cute chibi village life style, pastel colors, rounded characters, cozy nature"
```

---

## 10. 감정 시스템

### EmotionType Enum
```java
JOY("기쁨", "joy"),
ANXIETY("불안", "anxiety"),
ANGER("분노", "anger"),
SADNESS("슬픔", "sadness"),
SURPRISE("놀라움", "surprise"),   ← DISCOMFORT에서 변경됨
PEACE("평온", "peace")
```

### 감정별 시스템 메시지
```java
JOY   → "좋은 꿈을 꾸셨군요! 어떤 점이 가장 즐거우셨나요?"
ANXIETY → "불안한 꿈이셨군요. 어떤 부분이 가장 불안하셨나요?"
ANGER   → "화가 나는 꿈이셨군요. 무엇이 가장 화나셨나요?"
SADNESS → "슬픈 꿈이셨군요. 어떤 점이 가장 슬프셨나요?"
SURPRISE → "놀라운 꿈이셨군요. 어떤 점이 가장 놀라우셨나요?"
PEACE   → "평온한 꿈이셨군요. 어떤 느낌이 드셨나요?"
```

---

## 11. 성능 최적화 전략

### ⚡ **비동기 처리**
- AI API 호출은 모두 `@Async`로 처리
- 사용자는 즉시 응답 받고, 백그라운드에서 처리
- 폴링으로 진행 상태 업데이트

### 💰 **비용 최적화**
- 시스템 메시지는 하드코딩 (API 호출 불필요)
- 개발/테스트: `gpt-image-1` quality=low 사용
- DB 커넥션 최소화: TransactionTemplate으로 3구간에만 커넥션 사용

### 🗄️ **캐싱 전략**
- GCS 이미지 CDN 활용
- 자주 조회되는 꿈은 Redis 캐싱 고려

---

## 12. 개발 우선순위 (Roadmap)

### ✅ **완료 (v1.0)**
1. 꿈 플로우 전체 API (PENDING → ANALYZING → ANALYSIS_COMPLETED → GENERATING → COMPLETED)
2. GPT-4o 꿈 분석 비동기 처리
3. DALL-E / GPT-Image-1 4컷 웹툰 생성 (프로바이더 추상화)
4. GCS 이미지 저장
5. 라이브러리 / 즐겨찾기 / 챗봇 API
6. Character DNA 기반 캐릭터 일관성
7. content policy 재시도 로직

### 🔧 **진행 중 (v1.1)**
1. `emotionScores` 키 통일 (`놀라움` ← `놀람` 수정 완료)
2. `GlobalExceptionHandler` NoResourceFoundException WARN 처리 완료
3. `GptImageProvider` 에러 메시지 개선 완료

### 🚧 **다음 작업 (v1.2)**
1. **SubscriptionTier** FREE/PLUS/PRO/ULTRA 4단계 확장
2. **Subscription 엔티티** 스탠다드/프리미엄 카운터 분리 + premiumTrialUsed 플래그
3. **Genre enum** MARVEL, LEGO, ANIMAL_CROSSING 추가 + isPremium 플래그
4. **비회원 API** 별도 엔드포인트 구현
5. **Polar.sh Webhook** 핸들러 + 구독 동기화
6. **관리자 API** 티어 강제 변경 (테스트용)
7. **쿼터 체크 로직** generateWebtoon 시 스탠다드/프리미엄 분기

---

## 13. 예상 비용 산정

| 항목 | 비용 | 비고 |
|------|------|------|
| GPT-4o 꿈 분석 | ~$0.01 | 약 1,000 토큰 |
| GPT-Image-1 4컷 생성 (medium) | ~$0.12 | $0.03 × 4장 |
| 심리상담 챗봇 (10턴) | ~$0.02 | 선택적 기능 |
| **총 비용** | **~$0.15** | 꿈 1개당 |

→ 비용 절감 옵션: 4패널 단일 프롬프트 사용 시 ~$0.03 (75% 절감)

---

**Last Updated**: 2026-02-19
**Version**: 3.0 (4단계 구독, Polar.sh, 필터 시스템 반영)
