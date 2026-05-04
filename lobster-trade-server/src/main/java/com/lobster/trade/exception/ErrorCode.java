package com.lobster.trade.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum ErrorCode {
    PARAM_INVALID(400, "参数错误"),
    BAD_REQUEST(400, "请求错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() { return code; }
    public String getMessage() { return message; }
}
