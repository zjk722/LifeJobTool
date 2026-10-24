package com.homeservice.auth.service;

import com.homeservice.auth.dto.LoginResponse;
import com.homeservice.auth.dto.SmsLoginRequest;
import com.homeservice.auth.security.CurrentUser;
import com.homeservice.auth.security.UserContext;
import com.homeservice.common.exception.BizException;
import com.homeservice.user.domain.User;
import com.homeservice.user.domain.UserRole;
import com.homeservice.user.domain.UserStatus;
import com.homeservice.user.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SmsCodeService smsCodeService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void existingUserLoginReturnsToken() {
        User user = User.builder()
                .id(1L)
                .phone("13800138000")
                .nickname("张三")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.NORMAL)
                .build();
        when(userMapper.selectOne(any())).thenReturn(user);
        when(jwtService.issue(1L, UserRole.CUSTOMER)).thenReturn(
                new JwtService.IssuedToken("token", "jti", Instant.now().plusSeconds(7200), 7200));

        LoginResponse response = authService.loginBySms(
                new SmsLoginRequest("13800138000", "123456", UserRole.CUSTOMER));

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.user().id()).isEqualTo(1L);
        verify(smsCodeService).verify("13800138000", "123456");
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void newCustomerIsAutoRegistered() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });
        when(jwtService.issue(anyLong(), eq(UserRole.CUSTOMER))).thenReturn(
                new JwtService.IssuedToken("token-2", "jti-2", Instant.now().plusSeconds(7200), 7200));

        LoginResponse response = authService.loginBySms(
                new SmsLoginRequest("13800138000", "123456", UserRole.CUSTOMER));

        assertThat(response.token()).isEqualTo("token-2");
        assertThat(response.user().phone()).isEqualTo("13800138000");
        assertThat(response.user().role()).isEqualTo(UserRole.CUSTOMER);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void newWorkerIsRejected() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.loginBySms(
                new SmsLoginRequest("13800138000", "123456", UserRole.WORKER)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("入驻");

        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void disabledUserIsRejected() {
        User user = User.builder()
                .id(3L)
                .phone("13800138000")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.DISABLED)
                .build();
        when(userMapper.selectOne(any())).thenReturn(user);

        assertThatThrownBy(() -> authService.loginBySms(
                new SmsLoginRequest("13800138000", "123456", UserRole.CUSTOMER)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("禁用");
    }

    @Test
    void logoutBlacklistsCurrentToken() {
        UserContext.set(new CurrentUser(1L, UserRole.CUSTOMER, "jti", Instant.now().plusSeconds(60)));

        authService.logout();

        verify(tokenBlacklistService).blacklist(eq("jti"), any(Instant.class));
    }
}
