package com.homeservice.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.homeservice.auth.dto.LoginResponse;
import com.homeservice.auth.dto.SmsLoginRequest;
import com.homeservice.auth.dto.UserInfo;
import com.homeservice.auth.security.CurrentUser;
import com.homeservice.auth.security.UserContext;
import com.homeservice.common.exception.BizException;
import com.homeservice.common.exception.ErrorCode;
import com.homeservice.user.domain.User;
import com.homeservice.user.domain.UserRole;
import com.homeservice.user.domain.UserStatus;
import com.homeservice.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SmsCodeService smsCodeService;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public void sendSmsCode(String phone) {
        smsCodeService.sendLoginCode(phone);
    }

    public LoginResponse loginBySms(SmsLoginRequest request) {
        smsCodeService.verify(request.phone(), request.code());

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getPhone, request.phone()));

        if (user == null) {
            if (request.role() != UserRole.CUSTOMER) {
                throw new BizException(ErrorCode.FORBIDDEN, "该手机号还没有服务人员账号，请先提交入驻申请");
            }
            user = User.builder()
                    .phone(request.phone())
                    .nickname("用户" + request.phone().substring(7))
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.NORMAL)
                    .build();
            try {
                userMapper.insert(user);
            } catch (DuplicateKeyException ex) {
                // 并发下同一个新手机号可能同时注册，唯一索引兜底后回查一次
                user = userMapper.selectOne(
                        Wrappers.<User>lambdaQuery().eq(User::getPhone, request.phone()));
                if (user == null) {
                    throw ex;
                }
            }
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }

        JwtService.IssuedToken issued = jwtService.issue(user.getId(), user.getRole());
        return new LoginResponse(issued.token(), issued.expiresIn(), UserInfo.from(user));
    }

    public UserInfo me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return UserInfo.from(user);
    }

    public void logout() {
        CurrentUser current = UserContext.get();
        if (current == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        tokenBlacklistService.blacklist(current.jti(), current.expiresAt());
    }
}
