package com.homeservice.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsSender implements SmsSender {

    @Override
    public void sendLoginCode(String phone, String code) {
        log.info("[MockSms] 登录验证码 phone={}, code={}", phone, code);
    }
}
