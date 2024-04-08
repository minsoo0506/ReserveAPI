package com.mnsoo.reservation.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// JwtAuthenticationFilter는 HTTP 요청이 들어올 때마다 JWT 토큰의 유효성을 검증하는 필터.
// 이 필터는 OncePerRequestFilter를 상속받아, HTTP 요청이 올 때마다 한 번씩 실행됨.
// 토큰의 유효성을 검증한 후, 해당 토큰에서 사용자의 역할을 추출하고, 이를 기반으로 인증 객체를 생성하여
// SecurityContext에 설정. 이후 처리 과정에서 이 인증 정보를 사용할 수 있음.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer "; // 인증 타입

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = this.resolveTokenFromRequest(request);

        if(StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
            // 토큰 유효성 검증
            String role = this.tokenProvider.getRolesFromToken(token);
            System.out.println(role);
            Authentication auth = null;

            if ("ROLE_PARTNER".equals(role)) {
                auth = this.tokenProvider.getPartnerAuthentication(token);
            } else if ("ROLE_RESERVER".equals(role)) {
                auth = this.tokenProvider.getReserverAuthentication(token);
            }

            if (auth == null) {
                throw new IllegalArgumentException("Invalid role in token");
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if(!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)){
            return token.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}
