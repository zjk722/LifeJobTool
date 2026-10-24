package com.homeservice.common.web;

import com.homeservice.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PingController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("app", appName);
        body.put("java", System.getProperty("java.version"));
        body.put("time", LocalDateTime.now().toString());
        return ApiResponse.ok(body);
    }
}
