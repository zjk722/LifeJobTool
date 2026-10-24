package com.homeservice.common.exception;

public enum ErrorCode {

    BAD_REQUEST(40000, "请求参数错误", 400),
    UNAUTHORIZED(40100, "未登录或登录已过期", 401),
    FORBIDDEN(40300, "无权访问", 403),
    NOT_FOUND(40400, "资源不存在", 404),
    CONFLICT(40900, "资源冲突", 409),
    TOO_MANY_REQUESTS(42900, "请求过于频繁", 429),
    INTERNAL_ERROR(50000, "系统内部错误", 500),

    ORDER_STATE_INVALID(41001, "订单状态不允许该操作", 409),
    SLOT_NOT_AVAILABLE(41002, "时间槽已被占用", 409),
    SMS_CODE_EXPIRED(41003, "验证码已过期", 400),
    SMS_CODE_INVALID(41004, "验证码错误", 400),
    USER_DISABLED(41005, "账号已被禁用", 403);

    private final int code;
    private final String message;
    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
