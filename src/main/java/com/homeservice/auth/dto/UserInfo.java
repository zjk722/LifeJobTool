package com.homeservice.auth.dto;

import com.homeservice.user.domain.User;
import com.homeservice.user.domain.UserRole;

public record UserInfo(Long id, String name, String phone, UserRole role, String avatar) {

    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getNickname(),
                user.getPhone(),
                user.getRole(),
                user.getAvatar());
    }
}
