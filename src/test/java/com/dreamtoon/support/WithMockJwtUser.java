package com.dreamtoon.support;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * JWT 인증이 필요한 API 테스트에서 사용하는 커스텀 어노테이션.
 *
 * <p>실제 JWT 필터와 동일하게 SecurityContext의 principal을 {@link Long} userId로 설정합니다.
 * {@code @WithMockUser(username = "1")}는 principal이 String이라 컨트롤러의 {@code @AuthenticationPrincipal
 * Long userId}와 타입이 다를 수 있으므로, 본 어노테이션을 사용하면 프로덕션과 동일한 타입으로 테스트할 수 있습니다.
 *
 * <p>사용 예:
 *
 * <pre>
 * &#64;Test
 * &#64;WithMockJwtUser(userId = 1L)
 * void whenAuthenticated_thenReturnsDream() throws Exception {
 *     mockMvc.perform(get("/api/v1/dreams/1"))
 *             .andExpect(status().isOk());
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtUserSecurityContextFactory.class)
public @interface WithMockJwtUser {

    long userId() default 1L;

    String role() default "ROLE_USER";
}
