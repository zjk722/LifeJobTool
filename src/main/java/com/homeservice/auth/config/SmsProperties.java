package com.homeservice.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    private String provider = "mock";

    private String signName;

    private String templateCode;
}
