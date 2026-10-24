package com.homeservice.common.api;

import org.slf4j.MDC;

public record ApiResponse<T>(int code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data, MDC.get("traceId"));
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "ok", null, MDC.get("traceId"));
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, MDC.get("traceId"));
    }
}
