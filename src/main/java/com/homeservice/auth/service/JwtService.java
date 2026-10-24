package com.homeservice.auth.service;

import com.homeservice.auth.config.JwtProperties;
import com.homeservice.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret 不能为空");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secret 至少需要 32 字节");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public IssuedToken issue(Long userId, UserRole role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getExpireSeconds());
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .id(jti)
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();

        return new IssuedToken(token, jti, expiresAt, properties.getExpireSeconds());
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record IssuedToken(String token, String jti, Instant expiresAt, long expiresIn) {
    }
}
