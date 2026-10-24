package com.homeservice.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;

    private long expireSeconds = 7200;

    private String issuer = "home-service";
}
