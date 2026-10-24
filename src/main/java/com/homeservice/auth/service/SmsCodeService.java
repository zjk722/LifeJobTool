package com.homeservice.auth.service;

import com.homeservice.auth.sms.SmsSender;
import com.homeservice.common.exception.BizException;
import com.homeservice.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private static final String CODE_KEY = "sms:code:login:%s";
    private static final String COOLDOWN_KEY = "sms:cooldown:%s";
    private static final String ATTEMPT_KEY = "sms:attempt:%s";

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 校验并消费验证码，整个过程在 Redis 里原子完成：
     * -1 已过期，0 错误，1 成功。
     */
    private static final DefaultRedisScript<Long> VERIFY_SCRIPT = new DefaultRedisScript<>("""
            local code = redis.call('GET', KEYS[1])
            if not code then
                return -1
            end
            if code ~= ARGV[1] then
                local attempts = redis.call('INCR', KEYS[2])
                if attempts == 1 then
                    redis.call('EXPIRE', KEYS[2], 300)
                end
                if attempts >= tonumber(ARGV[2]) then
                    redis.call('DEL', KEYS[1])
                end
                return 0
            end
            redis.call('DEL', KEYS[1])
            redis.call('DEL', KEYS[2])
            return 1
            """, Long.class);

    private final StringRedisTemplate redis;
    private final SmsSender smsSender;

    public void sendLoginCode(String phone) {
        Boolean acquired = redis.opsForValue()
                .setIfAbsent(COOLDOWN_KEY.formatted(phone), "1", COOLDOWN_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS, "发送太频繁，请稍后再试");
        }

        String code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        String codeKey = CODE_KEY.formatted(phone);
        redis.opsForValue().set(codeKey, code, CODE_TTL);
        redis.delete(ATTEMPT_KEY.formatted(phone));

        try {
            smsSender.sendLoginCode(phone, code);
        } catch (RuntimeException ex) {
            redis.delete(codeKey);
            redis.delete(COOLDOWN_KEY.formatted(phone));
            throw new BizException(ErrorCode.INTERNAL_ERROR, "验证码发送失败，请重试");
        }
    }

    public void verify(String phone, String code) {
        Long result = redis.execute(
                VERIFY_SCRIPT,
                List.of(CODE_KEY.formatted(phone), ATTEMPT_KEY.formatted(phone)),
                code,
                String.valueOf(MAX_ATTEMPTS));

        if (result == null) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
        if (result == -1L) {
            throw new BizException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (result == 0L) {
            throw new BizException(ErrorCode.SMS_CODE_INVALID);
        }
    }
}
