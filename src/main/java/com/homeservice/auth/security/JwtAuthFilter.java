package com.homeservice.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeservice.auth.service.JwtService;
import com.homeservice.auth.service.TokenBlacklistService;
import com.homeservice.common.api.ApiResponse;
import com.homeservice.common.exception.ErrorCode;
import com.homeservice.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtService.parse(token);
            String jti = claims.getId();
            String roleValue = claims.get("role", String.class);
            if (jti == null || roleValue == null
                    || claims.getSubject() == null || claims.getExpiration() == null
                    || tokenBlacklistService.isBlacklisted(jti)) {
                writeUnauthorized(response);
                return;
            }

            UserContext.set(new CurrentUser(
                    Long.valueOf(claims.getSubject()),
                    UserRole.valueOf(roleValue),
                    jti,
                    claims.getExpiration().toInstant()));
        } catch (JwtException | IllegalArgumentException ex) {
            writeUnauthorized(response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), "登录已过期，请重新登录")));
    }
}
