package com.homeservice.auth.service;

import com.homeservice.auth.sms.SmsSender;
import com.homeservice.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsCodeServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SmsSender smsSender;

    @InjectMocks
    private SmsCodeService smsCodeService;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void sendLoginCodeStoresCodeAndCallsSender() {
        when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);

        smsCodeService.sendLoginCode("13800138000");

        verify(valueOperations).set(
                startsWith("sms:code:login:13800138000"), anyString(), any(Duration.class));
        verify(smsSender).sendLoginCode(eq("13800138000"), anyString());
    }

    @Test
    void sendLoginCodeTooFrequent() {
        when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> smsCodeService.sendLoginCode("13800138000"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("频繁");

        verifyNoInteractions(smsSender);
    }
}
