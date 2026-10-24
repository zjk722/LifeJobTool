package com.homeservice.auth.service;

import com.homeservice.auth.config.JwtProperties;
import com.homeservice.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(properties("test-secret-0123456789abcdef0123456789abcdef"));
    }

    @Test
    void issueAndParse() {
        JwtService.IssuedToken issued = jwtService.issue(42L, UserRole.CUSTOMER);

        Claims claims = jwtService.parse(issued.token());

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.getId()).isNotBlank();
        assertThat(issued.expiresIn()).isEqualTo(7200);
    }

    @Test
    void tokenSignedByAnotherSecretIsRejected() {
        JwtService other = new JwtService(properties("another-secret-0123456789abcdef0123456789abcdef"));
        String token = other.issue(1L, UserRole.CUSTOMER).token();

        assertThatThrownBy(() -> jwtService.parse(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void shortSecretIsRejected() {
        assertThatThrownBy(() -> new JwtService(properties("short")))
                .isInstanceOf(IllegalStateException.class);
    }

    private JwtProperties properties(String secret) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setExpireSeconds(7200);
        properties.setIssuer("home-service-test");
        return properties;
    }
}
