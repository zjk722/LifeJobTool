package com.homeservice.auth.sms;

import com.homeservice.auth.config.SmsProperties;
import com.homeservice.common.exception.BizException;
import com.homeservice.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sms.provider", havingValue = "aliyun")
public class AliyunSmsSender implements SmsSender {

    private final SmsProperties properties;

    @Override
    public void sendLoginCode(String phone, String code) {
        // TODO 接入阿里云 dysmsapi20170525，使用 properties.getSignName() / getTemplateCode()
        log.warn("阿里云短信通道尚未配置, phone={}, sign={}", phone, properties.getSignName());
        throw new BizException(ErrorCode.INTERNAL_ERROR, "短信通道未配置");
    }
}
