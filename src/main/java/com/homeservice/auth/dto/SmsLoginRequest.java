package com.homeservice.auth.dto;

import com.homeservice.user.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SmsLoginRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码为 6 位数字")
        String code,

        UserRole role) {

    public SmsLoginRequest {
        if (role == null) {
            role = UserRole.CUSTOMER;
        }
    }
}
