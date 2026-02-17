package com.dreamtoon.support;

import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * {@link WithMockJwtUser}용 SecurityContext 생성기.
 *
 * <p>principal을 Long userId로 설정하여 JwtAuthenticationFilter와 동일한 형태로 컨트롤러에서
 * {@code @AuthenticationPrincipal Long userId}가 주입되도록 합니다.
 */
public class WithMockJwtUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockJwtUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        annotation.userId(),
                        null,
                        Collections.singletonList(
                                new org.springframework.security.core.authority
                                        .SimpleGrantedAuthority(annotation.role())));
        context.setAuthentication(auth);
        return context;
    }
}
