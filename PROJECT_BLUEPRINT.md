# 🚀 PROJECT_BLUEPRINT: DreamToon (드림툰)

> **AI 기반 꿈 시각화 및 정서 상태 분석 헬스케어 플랫폼**
> 본 문서는 해커톤 MVP 개발을 위한 기술 설계 및 기획 가이드라인입니다.

---

## 1. 프로젝트 개요 (Overview)

- **핵심 가치**: 휘발되는 꿈 데이터를 시각적 콘텐츠(4컷 만화)로 변환하고, 무의식 속 건강 지표를 도출함.
- **주요 타겟**: 기록의 재미를 느끼고 싶은 사용자, 심리적 불안정감을 해소하고 싶은 현대인.
- **차별점**:
    - 단계별 인터랙티브 꿈 입력 플로우 (꿈 내용 → 감정 선택 → 상세 설명 → 장르 선택)
    - GPT-4 기반 꿈 분석 및 감정 레이더 차트 생성
    - DALL-E 기반 장르별 4컷 만화 자동 생성
    - 심리상담사 페르소나 AI 챗봇을 통한 꿈 심층 상담
    - 개인 라이브러리를 통한 꿈 아카이빙 및 패턴 분석

---

## 2. 기술 스택 (Technical Stack)

| Category | Technology | Reason |
| :--- | :--- | :--- |
| **Backend** | **Spring Boot 3.2+** | 익숙한 생태계, 안정적인 아키텍처 및 확장성 확보 |
| **AI Library** | **Spring AI (OpenAI)** | GPT-4, DALL-E 통합 API 호출 간소화 |
| **Database** | **PostgreSQL** | JSONB 지원으로 감정 분석 데이터 및 채팅 내역 저장 용이 |
| **Storage** | **AWS S3** | 생성된 4컷 만화 이미지 호스팅 |
| **Async Processing** | **Spring @Async** | 비동기 AI API 호출로 사용자 대기 시간 최소화 |
| **Frontend** | **Next.js** | 인터랙티브 UI 및 웹툰 뷰어 구현 최적화 |

---

## 3. 핵심 사용자 플로우 (User Flow)

### 📱 **Phase 1: 꿈 입력 및 감정 선택**

```
[메인 화면]
  ↓ 사용자 입력: "나 어제 썸녀와 데이트하는 꿈 꿨어"

[감정 선택 화면]
  시스템 메시지: "안녕하세요! 어젯밤 꾸셨던 꿈은 어떠셨나요?"
  ↓ 사용자 선택: 😊 기쁨 / 😢 불안 / 😤 분노 / 😰 슬픔 / 🤔 불편 / 😑 평온

[상세 설명 입력]
  시스템 메시지 (감정별 매핑): "좋은 꿈을 꾸셨군요! 어떤 점이 가장 즐거우셨나요?"
  ↓ 사용자 입력: "카페에서 데이트했는데 분위기가 너무 좋았어요"
  ↓ (선택) 현실 상황 고민: "요즘 그 사람한테 고백할까 고민중이에요"
```

### 🤖 **Phase 2: AI 꿈 분석 (비동기)**

```
[백엔드 처리]
  → GPT-4 API 호출
  → 입력 데이터:
     - 꿈 내용
     - 선택한 감정
     - 상세 설명
     - 현실 고민 (optional)

  → 출력 데이터:
     - 꿈 해석 텍스트
     - 감정 레이더 차트 점수 (기쁨, 불안, 분노, 슬픔, 불편, 평온)
     - AI 인사이트 메시지
```

### 🎨 **Phase 3: 장르 선택 및 4컷 만화 생성**

```
[장르 선택 화면]
  사용자 선택: 로맨스 / 판타지 / 힐링 / 호러

[백엔드 처리]
  → DALL-E API 호출 (4회)
  → 각 컷마다 장르에 맞는 프롬프트 생성
  → S3에 이미지 업로드

[완성 화면]
  - 4컷 만화 표시
  - AI 생성 제목 (예: "무의식의 숲을 지나서")
  - 생성 날짜
  - [라이브러리에 등록하기] 버튼
  - [새로운 채팅] 버튼
  - [꿈 더 대화하기] 버튼
```

### 💬 **Phase 4: 심리상담 챗봇 (선택)**

```
[꿈 더 대화하기 클릭]
  → 심리상담사 페르소나 GPT 챗봇 시작
  → 해당 꿈에 대한 심층 상담
  → 채팅 내역 DB 저장
```

### 📚 **Phase 5: 라이브러리 관리**

```
[라이브러리 화면]
  - 검색: 제목/내용으로 검색
  - 필터링:
    ✅ 즐겨찾기
    ✅ 최신순 정렬
    ✅ 장르별 필터 (로맨스/판타지/힐링/호러)

  - 각 꿈 카드:
    - 썸네일 (4컷 중 첫 번째 이미지)
    - 제목
    - 날짜
    - 장르 태그
    - 즐겨찾기 토글
```

---

## 4. 시스템 아키텍처 (System Architecture)

### 🔄 **비동기 처리 전략**

```
사용자 요청 → 즉시 202 Accepted 응답
              ↓
         비동기 작업 시작
              ↓
    ┌─────────┴─────────┐
    │                   │
 GPT-4 분석        DALL-E 생성
    │                   │
    └─────────┬─────────┘
              ↓
         DB 저장 완료
              ↓
    status: COMPLETED
```

### 💾 **데이터 저장 전략**

- **시스템 메시지**: 하드코딩 (감정별 매핑 Map)
- **사용자 입력**: 모두 DB 저장 (꿈 내용, 감정, 상세 설명, 현실 고민)
- **AI 생성 데이터**: PostgreSQL JSONB 활용
- **이미지**: S3 저장 후 URL만 DB에 저장

---

## 5. 데이터 모델링 (Entity Design)

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
- primary_emotion (Enum): 기쁨, 불안, 분노, 슬픔, 불편, 평온
- detailed_description (Text): 상세 설명
- real_life_context (Text, nullable): 현실 고민

// AI 생성 데이터
- title (String): AI가 생성한 제목
- ai_analysis (Text): 꿈 해석
- emotion_scores (Jsonb): {기쁨: 85, 불안: 20, ...}
- ai_insight (String): AI 코칭 메시지

// 웹툰 관련
- selected_genre (Enum): 로맨스, 판타지, 힐링, 호러
- webtoon_images (Jsonb): ["s3://url1", "s3://url2", ...]

// 라이브러리 관련
- is_favorite (Boolean): 즐겨찾기 여부
- is_in_library (Boolean): 라이브러리 등록 여부

// 처리 상태
- processing_status (Enum): PENDING, ANALYZING, GENERATING, COMPLETED, FAILED

- created_at (DateTime)
- updated_at (DateTime)
```

### 💬 **DreamChat Entity** (꿈 더 대화하기)
```java
- id (Long, PK)
- dream_id (Long, FK)
- role (Enum): USER, ASSISTANT
- message (Text)
- created_at (DateTime)
```

---

### 📈 **AnalysisStats (Dashboard Cache)**
대시보드 로딩 속도를 위해 매번 계산하지 않고 일별/주별 통계를 요약 저장 (선택 사항)
```java
- id (Long)
- user_id (Long)
- date (Date)
- average_stress_level (Integer)
- dominant_emotion (Enum)
- sleep_score (Integer)
```

---

## 6. 주요 API 명세 (API Specification)

### **[POST] /api/v1/dreams**
꿈 내용 최초 입력

**Request:**
```json
{
  "dreamContent": "나 어제 썸녀와 데이트하는 꿈 꿨어"
}
```

**Response:** `201 Created`
```json
{
  "dreamId": 123,
  "systemMessage": "안녕하세요! 어젯밤 꾸셨던 꿈은 어떠셨나요?"
}
```

---

### **[PATCH] /api/v1/dreams/{dreamId}/emotion**
감정 선택

**Request:**
```json
{
  "primaryEmotion": "기쁨"
}
```

**Response:** `200 OK`
```json
{
  "systemMessage": "좋은 꿈을 꾸셨군요! 어떤 점이 가장 즐거우셨나요?"
}
```

---

### **[PATCH] /api/v1/dreams/{dreamId}/details**
상세 설명 입력 및 AI 분석 시작

**Request:**
```json
{
  "detailedDescription": "카페에서 데이트했는데 분위기가 너무 좋았어요",
  "realLifeContext": "요즘 그 사람한테 고백할까 고민중이에요"  // optional
}
```

**Response:** `202 Accepted`
```json
{
  "dreamId": 123,
  "status": "ANALYZING",
  "message": "꿈을 분석하고 있어요. 잠시만 기다려주세요!"
}
```

---

### **[GET] /api/v1/dreams/{dreamId}/analysis**
분석 결과 조회

**Response:** `200 OK`
```json
{
  "dreamId": 123,
  "status": "ANALYSIS_COMPLETED",
  "aiAnalysis": "당신의 꿈은 현실에서의 로맨틱한 욕구를 반영하고 있습니다...",
  "emotionScores": {
    "기쁨": 85,
    "불안": 20,
    "분노": 5,
    "슬픔": 10,
    "불편": 15,
    "평온": 70
  },
  "aiInsight": "긍정적인 감정이 지배적인 꿈이었습니다. 현실에서도 좋은 일이 있을 것 같아요!"
}
```

---

### **[POST] /api/v1/dreams/{dreamId}/webtoon**
장르 선택 및 4컷 만화 생성 시작

**Request:**
```json
{
  "selectedGenre": "로맨스"
}
```

**Response:** `202 Accepted`
```json
{
  "dreamId": 123,
  "status": "GENERATING",
  "message": "4컷 만화를 생성하고 있어요. 조금만 기다려주세요!"
}
```

---

### **[GET] /api/v1/dreams/{dreamId}**
전체 꿈 데이터 조회 (완성본)

**Response:** `200 OK`
```json
{
  "dreamId": 123,
  "title": "무의식의 숲을 지나서",
  "dreamContent": "나 어제 썸녀와 데이트하는 꿈 꿨어",
  "primaryEmotion": "기쁨",
  "detailedDescription": "카페에서...",
  "realLifeContext": "요즘 그 사람한테...",
  "aiAnalysis": "...",
  "emotionScores": {...},
  "selectedGenre": "로맨스",
  "webtoonImages": [
    "https://s3.amazonaws.com/.../panel1.png",
    "https://s3.amazonaws.com/.../panel2.png",
    "https://s3.amazonaws.com/.../panel3.png",
    "https://s3.amazonaws.com/.../panel4.png"
  ],
  "status": "COMPLETED",
  "isFavorite": false,
  "isInLibrary": false,
  "createdAt": "2026-02-17T18:30:00"
}
```

---

### **[POST] /api/v1/dreams/{dreamId}/library**
라이브러리에 등록

**Response:** `200 OK`
```json
{
  "dreamId": 123,
  "isInLibrary": true
}
```

---

### **[PATCH] /api/v1/dreams/{dreamId}/favorite**
즐겨찾기 토글

**Response:** `200 OK`
```json
{
  "dreamId": 123,
  "isFavorite": true
}
```

---

### **[GET] /api/v1/library**
라이브러리 조회 (필터링)

**Query Parameters:**
- `favorite` (boolean): 즐겨찾기만 조회
- `genre` (string): 로맨스, 판타지, 힐링, 호러
- `sort` (string): latest (기본값)
- `search` (string): 제목/내용 검색

**Response:** `200 OK`
```json
{
  "dreams": [
    {
      "dreamId": 123,
      "title": "무의식의 숲을 지나서",
      "thumbnailUrl": "https://s3.../panel1.png",
      "genre": "로맨스",
      "isFavorite": true,
      "createdAt": "2026-02-17"
    },
    ...
  ],
  "totalCount": 47
}
```

---

### **[POST] /api/v1/dreams/{dreamId}/chat**
꿈 더 대화하기 (심리상담 챗봇)

**Request:**
```json
{
  "message": "이 꿈이 무슨 의미인가요?"
}
```

**Response:** `200 OK`
```json
{
  "role": "assistant",
  "message": "이 꿈은 당신의 내면에 있는 로맨틱한 욕구를 반영하고 있습니다. 현실에서도...",
  "createdAt": "2026-02-17T18:35:00"
}
```

---

### **[GET] /api/v1/dreams/{dreamId}/chat**
채팅 내역 조회

**Response:** `200 OK`
```json
{
  "dreamId": 123,
  "chatHistory": [
    {
      "role": "user",
      "message": "이 꿈이 무슨 의미인가요?",
      "createdAt": "2026-02-17T18:35:00"
    },
    {
      "role": "assistant",
      "message": "이 꿈은...",
      "createdAt": "2026-02-17T18:35:05"
    }
  ]
}
```

---

### **[GET] /api/v1/analysis/dashboard**
꿈 분석 대시보드 (종합 건강 상태)

**Query Parameters:**
- `period`: 7d (기본값), 30d

**Response:** `200 OK`
```json
{
  "stressIndex": 75,
  "stressLevel": "HIGH", // LOW, MEDIUM, HIGH
  "sleepQualityScore": 60,
  "sleepQualityMessage": "수면의 질 개선이 필요해요.",
  "emotionBalance": {
    "기쁨": 30,
    "평온": 20,
    "불안": 60,
    "슬픔": 40,
    "분노": 10
  },
  "weeklyDreamFlow": [
    { "date": "2026-02-12", "hasDream": true, "primaryEmotion": "불안" },
    { "date": "2026-02-13", "hasDream": true, "primaryEmotion": "평온" },
    { "date": "2026-02-14", "hasDream": false, "primaryEmotion": null }
  ],
  "aiCoachMessage": "최근 불안과 관련된 꿈이 잦습니다. 잠들기 전 명상을 추천드려요."
}
```

**Response (데이터 없음 - 첫 사용자):** `200 OK`
```json
{
  "hasEnoughData": false,
  "message": "아직 분석할 꿈 데이터가 없어요. 첫 번째 꿈을 기록해보세요!",
  "stressIndex": 0,
  "sleepQualityScore": 0,
  "emotionBalance": null,
  "weeklyDreamFlow": []
}
```

---

## 7. AI 프롬프트 전략 (Prompt Engineering)

### 🧠 **꿈 분석 프롬프트 (GPT-4)**

```
당신은 전문 심리상담사입니다. 사용자의 꿈을 분석하여 심리 상태를 파악하고 조언을 제공하세요.

[입력 데이터]
- 꿈 내용: {dreamContent}
- 주요 감정: {primaryEmotion}
- 상세 설명: {detailedDescription}
- 현실 고민: {realLifeContext}

[출력 형식]
{
  "analysis": "꿈 해석 (200자 이내)",
  "emotionScores": {
    "기쁨": 0-100,
    "불안": 0-100,
    "분노": 0-100,
    "슬픔": 0-100,
    "불편": 0-100,
    "평온": 0-100
  },
  "insight": "AI 코칭 메시지 (100자 이내)",
  "title": "꿈 제목 (10자 이내)"
}
```

### 🎨 **4컷 만화 생성 프롬프트 (DALL-E)**

```
[장르별 스타일 프리셋]
- 로맨스: "romantic webtoon style, soft pastel colors, dreamy atmosphere"
- 판타지: "fantasy webtoon style, vibrant colors, magical elements"
- 힐링: "healing webtoon style, warm colors, peaceful mood"
- 호러: "horror webtoon style, dark colors, eerie atmosphere"

[프롬프트 구조]
"Webtoon style illustration, {genre_preset}, {scene_description}, Korean manhwa art style, clean lines, professional digital art"
```

### 💬 **심리상담 챗봇 시스템 프롬프트**

```
당신은 따뜻하고 공감적인 심리상담사입니다.
사용자의 꿈에 대해 깊이 있는 대화를 나누며, 심리적 안정을 제공하세요.

[꿈 컨텍스트]
- 꿈 내용: {dreamContent}
- 분석 결과: {aiAnalysis}

[대화 규칙]
1. 공감적이고 따뜻한 톤 유지
2. 전문적이지만 친근한 언어 사용
3. 사용자의 감정을 존중하고 인정
4. 필요시 실질적인 조언 제공
```

---

## 8. 성능 최적화 전략

### ⚡ **비동기 처리**
- AI API 호출은 모두 `@Async`로 처리
- 사용자는 즉시 응답 받고, 백그라운드에서 처리
- 폴링 또는 WebSocket으로 진행 상태 업데이트

### 💰 **비용 최적화**
- 시스템 메시지는 하드코딩 (API 호출 불필요)
- 감정별 후속 질문도 Map으로 관리
- GPT API는 실제 분석/생성에만 사용

### 🗄️ **캐싱 전략**
- S3 이미지는 CloudFront CDN 사용
- 자주 조회되는 꿈은 Redis 캐싱 고려

---

## 9. 개발 우선순위 (MVP Roadmap)

### ✅ **Phase 1: 핵심 플로우 구현**
1. 꿈 입력 → 감정 선택 → 상세 설명 API
2. GPT-4 꿈 분석 비동기 처리
3. DALL-E 4컷 만화 생성
4. 완성 화면 조회 API

### ✅ **Phase 2: 라이브러리 기능**
1. 라이브러리 등록/조회
2. 즐겨찾기 기능
3. 장르별 필터링
4. 검색 기능

### ✅ **Phase 3: 심리상담 챗봇**
1. 꿈 더 대화하기 API
2. 채팅 내역 저장/조회
3. 심리상담사 페르소나 프롬프트 최적화


### ✅ **Phase 4: 대시보드 & 분석 (Dream Health Analysis)**
1. **Stress Index & Sleep Quality**
   - 개별 꿈의 부정적 감정(불안, 공포, 분노) 점수를 가중 평균하여 산출
   - 수면 만족도 입력 필드 추가 고려
2. **Emotion Balance (Radar Chart)**
   - 기간 내 모든 꿈의 감정 점수 합산 및 정규화
3. **Weekly Dream Flow**
   - 캘린더 형태의 감정 흐름 시각화

---

## 10. 주요 기술적 고려사항

### 🔐 **보안**
- OAuth2 소셜 로그인 (Google, Kakao)
  - **Hybrid Authentication Flow**:
    - **Access Token**: 리다이렉트 URL 쿼리 파라미터로 전달 (`?accessToken=...`) - 즉시 사용
    - **Refresh Token**: `HttpOnly; Secure; SameSite=None` 쿠키로 설정 - 보안 강화
    - **Frontend**: URL에서 Access Token 추출, Refresh Token은 쿠키로 자동 전송 (API 호출 시)
- JWT 토큰 기반 인증
- 개인 꿈 데이터 암호화 저장

### 📊 **모니터링**
- AI API 호출 성공률 추적
- 평균 생성 시간 모니터링
- 에러율 및 재시도 로직

### 🧪 **테스트 전략**
- AI API 모킹으로 단위 테스트
- 비동기 처리 통합 테스트
- E2E 테스트 (전체 플로우)

---

## 11. 감정별 시스템 메시지 매핑

```java
public static final Map<String, String> EMOTION_MESSAGES = Map.of(
    "기쁨", "좋은 꿈을 꾸셨군요! 어떤 점이 가장 즐거우셨나요?",
    "불안", "불안한 꿈이셨군요. 어떤 부분이 가장 불안하셨나요?",
    "분노", "화가 나는 꿈이셨군요. 무엇이 가장 화나셨나요?",
    "슬픔", "슬픈 꿈이셨군요. 어떤 점이 가장 슬프셨나요?",
    "불편", "불편한 꿈이셨군요. 어떤 부분이 불편하셨나요?",
    "평온", "평온한 꿈이셨군요. 어떤 느낌이 드셨나요?"
);
```

---

## 12. 사용자 등급별 권한 시스템 (User Tier & Permission System)

### 📊 **사용자 등급 개요**

| 구분 | 비회원 (Guest) | 무료 회원 (Free) | 프리미엄 (Premium) |
|------|----------------|------------------|-------------------|
| **가입 필요** | ❌ | ✅ | ✅ + 구독 |
| **목적** | 서비스 체험 | 기본 기능 제공 | 전체 기능 무제한 |

---

### 🎯 **기능별 권한 매트릭스**

| 기능 | 비회원 | 무료 회원 | 프리미엄 |
|------|--------|-----------|----------|
| **꿈 생성** | 1회 체험 | 월 10개 | ♾️ 무제한 |
| **장르 선택** | 로맨스, 힐링 | 로맨스, 힐링 | 전체 (로맨스, 판타지, 힐링, 호러) |
| **AI 꿈 분석** | 간략 버전 | 전체 버전 | 전체 버전 + 고급 인사이트 |
| **4컷 만화** | 워터마크 O | 워터마크 O | 워터마크 X, 고화질 |
| **라이브러리 저장** | ❌ 불가 | 최대 50개 | ♾️ 무제한 |
| **즐겨찾기** | ❌ 불가 | 최대 10개 | ♾️ 무제한 |
| **꿈 더 대화하기** | ❌ 불가 | 꿈당 3턴 제한 | ♾️ 무제한 대화 |
| **대시보드/통계** | ❌ 불가 | ❌ 불가 | ✅ 주간/월간 리포트 |
| **AI 코칭** | ❌ 불가 | ❌ 불가 | ✅ 개인화 코칭 |
| **우선 처리** | ❌ | ❌ | ✅ 빠른 생성 속도 |
| **과거 기록** | ❌ 불가 | ✅ 가능 | ✅ 가능 |

---

### 🏗️ **필요 Entity 필드**

#### **User Entity**
- `userType`: GUEST, FREE, PREMIUM
- `subscriptionStatus`: NONE, ACTIVE, EXPIRED, CANCELLED
- `subscriptionStartDate`, `subscriptionEndDate`

#### **UserQuota Entity** (할당량 관리)
- `monthlyDreamCount`: 현재 월 생성한 꿈 개수
- `libraryCount`: 라이브러리 저장 개수
- `favoriteCount`: 즐겨찾기 개수
- `quotaResetDate`: 다음 리셋 날짜 (매월 1일)

#### **DreamChat Entity**
- `turnNumber`: 대화 턴 번호 (무료 회원 3턴 제한 체크용)

---

### 🎯 **백엔드 핵심 처리 로직**

#### **1. 권한 체크 & 에러 응답**

모든 API 호출 시 사용자 등급을 확인하고, 권한이 없으면 **403 Forbidden + 명확한 에러 메시지** 반환

**예시 1: 꿈 생성 시도**
```
프론트: POST /api/v1/dreams
백엔드 처리:
  1. 사용자 등급 확인 (GUEST/FREE/PREMIUM)
  2. 무료 회원이고 이번 달 10개 초과?
     → 403 응답 + upgradeRequired: true
  3. 권한 있으면 → 꿈 생성 + monthlyDreamCount += 1
```

**에러 응답 형식:**
```json
{
  "error": "MONTHLY_QUOTA_EXCEEDED",
  "message": "이번 달 꿈 생성 횟수를 모두 사용했어요",
  "upgradeRequired": true,
  "quota": { "used": 10, "limit": 10, "resetDate": "2026-03-01" }
}
```

**예시 2: 판타지 장르 선택 시도**
```
프론트: POST /api/v1/dreams/123/webtoon {"genre": "판타지"}
백엔드 처리:
  1. 사용자 등급 확인
  2. FREE 또는 GUEST면?
     → 403 응답 + availableGenres 정보 제공
  3. PREMIUM이면 → 4컷 만화 생성 진행
```

**예시 3: 채팅 4번째 메시지 시도**
```
프론트: POST /api/v1/dreams/123/chat {"message": "더 알려줘"}
백엔드 처리:
  1. 사용자 등급 확인
  2. FREE 회원이면 → DB에서 해당 꿈의 USER 메시지 개수 조회
     SELECT COUNT(*) FROM dream_chat WHERE dream_id = 123 AND role = 'USER'
  3. 3개 이상이면 → 403 응답
  4. 3개 미만이면 → 채팅 저장 + turnNumber 증가
```

---

#### **2. 할당량 정보 제공 API**

프론트가 대시보드에 할당량 표시하려면 백엔드에서 데이터 제공 필요

**API: GET /api/v1/users/me/permissions**

```json
{
  "userType": "FREE",
  "quota": {
    "dreams": {
      "used": 7,
      "limit": 10,
      "remaining": 3
    },
    "library": {
      "used": 23,
      "limit": 50,
      "remaining": 27
    },
    "favorites": {
      "used": 5,
      "limit": 10,
      "remaining": 5
    },
    "resetDate": "2026-03-01"
  },
  "permissions": {
    "canCreateDream": true,
    "availableGenres": ["로맨스", "힐링"],
    "canAccessDashboard": false
  }
}
```

프론트는 이 데이터로 진행바 표시:
```
🌙 꿈 생성: 7/10 ████████░░
📚 라이브러리: 23/50 ████████░░░░░
⭐ 즐겨찾기: 5/10 █████░░░░░
```

---

#### **3. 할당량 증가/감소 처리**

사용자 행동마다 DB 할당량 업데이트

| 행동 | DB 업데이트 |
|------|------------|
| 꿈 생성 성공 | `UserQuota.monthlyDreamCount += 1` |
| 라이브러리 등록 | `UserQuota.libraryCount += 1` |
| 라이브러리 삭제 | `UserQuota.libraryCount -= 1` |
| 즐겨찾기 추가 | `UserQuota.favoriteCount += 1` |
| 즐겨찾기 해제 | `UserQuota.favoriteCount -= 1` |

---

#### **4. 채팅 턴 수 카운팅**

채팅 메시지마다 턴 번호 저장하고, 새 메시지 시 턴 수 체크

```
채팅 메시지 저장:
  → DreamChat 테이블에 저장
  → role: "USER" or "ASSISTANT"
  → turnNumber: 1, 2, 3, ...

새 채팅 요청:
  → SELECT COUNT(*) FROM dream_chat WHERE dream_id = ? AND role = 'USER'
  → 3개 이상이면 403 거부
```

---

#### **5. 매월 1일 할당량 리셋**

스케줄러로 자동 리셋 (무료 회원만)

```
매월 1일 00:00:
  → 모든 FREE 회원의 monthlyDreamCount = 0
  → quotaResetDate = 다음 달 1일로 업데이트
  → PREMIUM 회원은 리셋 불필요
```

---

### 📋 **백엔드 처리 요약표**

| 기능 | 백엔드 처리 내용 |
|------|-----------------|
| **권한 체크** | 모든 API 호출 시 사용자 등급 확인 → 권한 없으면 403 + 에러 메시지 |
| **할당량 정보** | GET /users/me/permissions API로 현재 사용량/남은량 제공 |
| **할당량 증가** | 꿈 생성, 라이브러리 등록 시 DB 카운트 +1 |
| **할당량 감소** | 라이브러리 삭제, 즐겨찾기 해제 시 DB 카운트 -1 |
| **채팅 턴 체크** | 채팅 메시지마다 턴 번호 저장, 새 메시지 시 턴 수 확인 후 3턴 초과 시 거부 |
| **월간 리셋** | 스케줄러로 매월 1일 무료 회원 할당량 초기화 |

---

### 💡 **프론트엔드 UX 전환 유도 (참고)**

> 아래는 프론트엔드에서 구현할 UX 전략입니다. 백엔드는 `upgradeRequired: true` 플래그와 할당량 정보만 제공하면 됩니다.

#### **비회원 → 회원 전환**
- 꿈 생성 완료 후 회원가입 유도 CTA 표시
- "무료 회원가입하고 이 꿈을 저장하세요!"

#### **무료 → 프리미엄 전환**
- 할당량 80% 도달 시 경고 배너
- 프리미엄 장르 선택 시 업그레이드 팝업
- 채팅 3턴 도달 시 프리미엄 유도 메시지

#### **대시보드 할당량 표시**
- 무료 회원 대시보드 상단에 진행바 표시
- 프리미엄 업그레이드 링크 제공

---

### 🎁 **프리미엄 혜택 비교표**

| 기능 | 무료 회원 | 프리미엄 |
|------|-----------|----------|
| 💭 **꿈 생성** | 월 10개 | ♾️ 무제한 |
| 🎨 **장르** | 로맨스, 힐링 (2종) | 전체 4종 |
| 💬 **AI 상담** | 꿈당 3턴 체험 | ♾️ 무제한 대화 |
| 📚 **라이브러리** | 최대 50개 | ♾️ 무제한 |
| ⭐ **즐겨찾기** | 최대 10개 | ♾️ 무제한 |
| 🖼️ **이미지 품질** | 워터마크 O | 워터마크 X, 고화질 |
| 📊 **대시보드** | ❌ | ✅ 주간/월간 리포트 |
| 🧠 **AI 코칭** | ❌ | ✅ 개인화 인사이트 |
| ⚡ **처리 속도** | 일반 | 우선 처리 |

---

## 13. 예상 비용 산정 (사용자 1명 기준)

| 항목 | 비용 | 비고 |
|------|------|------|
| GPT-4 꿈 분석 | ~$0.01 | 약 1,000 토큰 |
| DALL-E 4컷 생성 | ~$0.08 | $0.02 × 4장 |
| 심리상담 챗봇 (10턴) | ~$0.02 | 선택적 기능 |
| **총 비용** | **~$0.11** | 꿈 1개당 |

→ 월 1,000명 사용 시: **약 $110**

---

**Last Updated**: 2026-02-17
**Version**: 2.0 (새로운 플로우 반영)
