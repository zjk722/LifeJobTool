package com.homeservice.auth.controller;

import com.homeservice.auth.dto.LoginResponse;
import com.homeservice.auth.dto.SendSmsRequest;
import com.homeservice.auth.dto.SmsLoginRequest;
import com.homeservice.auth.dto.UserInfo;
import com.homeservice.auth.security.UserContext;
import com.homeservice.auth.service.AuthService;
import com.homeservice.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms/send")
    public ApiResponse<Void> sendSmsCode(@Valid @RequestBody SendSmsRequest request) {
        authService.sendSmsCode(request.phone());
        return ApiResponse.ok();
    }

    @PostMapping("/login/sms")
    public ApiResponse<LoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        return ApiResponse.ok(authService.loginBySms(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfo> me() {
        return ApiResponse.ok(authService.me(UserContext.requireUserId()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }
}
