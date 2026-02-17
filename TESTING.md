# DreamToon Backend 테스트 가이드

## 1. 단위 테스트 (Unit Test)

### 1.1 카카오/소셜 로그인 관련

| 대상 | 패키지 | 설명 |
|------|--------|------|
| **OAuthAttributes** | `infrastructure.security.OAuthAttributesTest` | 카카오/구글 attributes Map 파싱, 이메일 폴백, `toEntity()` 검증. 외부 API 없음. |
| **CustomOAuth2UserService** | `infrastructure.security.CustomOAuth2UserServiceTest` | `loadOAuth2User()` 스텁 후 최초 로그인(신규 저장)·재로그인(기존 User) 시나리오 검증. |
| **OAuth2AuthenticationSuccessHandler** | `infrastructure.security.OAuth2AuthenticationSuccessHandlerTest` | 로그인 성공 시 JWT 발급 호출 및 HTML 응답에 토큰·리다이렉트 URL 포함 여부 검증. |

실행:

```bash
./gradlew test --tests "com.dreamtoon.infrastructure.security.*"
```

### 1.2 인증이 필요한 API 테스트 (Controller / MockMvc)

컨트롤러는 `@AuthenticationPrincipal Long userId`를 사용하므로, **principal을 Long으로** 넣어주는 것이 좋습니다.

- **`@WithMockUser(username = "1")`**
  principal이 String `"1"`이라, 컨트롤러에서 Long으로 받을 때 환경에 따라 동작이 달라질 수 있음.
- **`@WithMockJwtUser(userId = 1L)`**
  principal을 Long `1L`로 설정해, 실제 JWT 필터와 동일한 형태로 테스트 가능.

예시:

```java
@Test
@DisplayName("인증 필요 API - JWT 사용자로 요청")
@WithMockJwtUser(userId = 1L)
void whenAuthenticated_thenReturnsOk() throws Exception {
    mockMvc.perform(get("/api/v1/dreams/1"))
            .andExpect(status().isOk());
}
```

여러 사용자로 나눠서 테스트할 때:

```java
@WithMockJwtUser(userId = 2L)
void whenOtherUser_thenForbidden() throws Exception { ... }
```

### 1.3 JWT Bearer 토큰으로 요청하고 싶을 때

실제 액세스 토큰을 만들어서 `Authorization: Bearer <token>`으로 보내고 싶다면:

1. 테스트 프로필에서 `JwtTokenProvider`를 빈으로 주입받고,
2. `jwtTokenProvider.createAccessToken(userId, email, "ROLE_USER")`로 토큰 생성,
3. `mockMvc.perform(get("/api/v1/dreams/1").header("Authorization", "Bearer " + accessToken))` 형태로 요청.

통합 테스트에서 사용자 생성 + 토큰 발급을 한 번에 쓰고 싶다면, 테스트용 `JwtTokenProvider`와 `UserRepository`를 이용해 토큰을 발급하는 헬퍼를 두고 재사용하면 됩니다.

## 2. 통합 테스트 (Integration Test)

- **DreamFlowIntegrationTest**
  꿈 생성 → 감정 선택 → 상세 입력 → 분석 → 웹툰 생성 → 라이브러리/즐겨찾기까지 한 번에 검증.
- `@ActiveProfiles("test")`, H2 in-memory DB 사용.
- 인증은 `@WithMockUser(username = "1")` 또는 `@WithMockJwtUser(userId = 1L)` 사용. `@BeforeEach`에서 `userRepository.save(testUser)`로 id=1 사용자를 넣어 두면 됨.

## 3. 테스트 프로필

- `src/test/resources/application-test.yml`
  - H2, JWT 설정(`jwt.secret`, `jwt.expiration`, `jwt.refresh-expiration`), OAuth2 client test 값 등.
- `@ActiveProfiles("test")` 로 로드.

## 4. 요약

1. **카카오 소셜 로그인**: `OAuthAttributesTest` → `CustomOAuth2UserServiceTest` → `OAuth2AuthenticationSuccessHandlerTest` 순으로 attributes 파싱, 저장/조회, JWT·리다이렉트까지 단위 테스트로 검증.
2. **인증 필요 서비스**: `@WithMockJwtUser(userId = 1L)`로 principal을 Long으로 맞추고, 필요 시 실제 JWT를 발급해 `Authorization: Bearer` 로 통합 테스트.
