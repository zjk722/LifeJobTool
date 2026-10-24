package com.homeservice.auth.security;

import com.homeservice.user.domain.UserRole;

import java.time.Instant;

public record CurrentUser(Long userId, UserRole role, String jti, Instant expiresAt) {
}
