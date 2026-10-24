package com.homeservice.auth.dto;

public record LoginResponse(String token, long expiresIn, UserInfo user) {
}
