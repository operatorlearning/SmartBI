package com.smartbi.common;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    SYSTEM_ERROR(500, "系统内部错误"),

    // 业务错误码
    USER_EXISTS(10001, "用户名已存在"),
    USER_NOT_FOUND(10002, "用户不存在"),
    PASSWORD_ERROR(10003, "密码错误"),
    USER_DISABLED(10004, "账号已被禁用"),

    DATASET_NOT_FOUND(20001, "数据集不存在"),
    DATASET_PARSE_ERROR(20002, "Excel解析失败"),
    DATASET_EMPTY(20003, "数据集为空"),
    FILE_TOO_LARGE(20004, "文件大小超出限制"),

    CHART_NOT_FOUND(30001, "图表不存在"),
    CHART_GEN_ERROR(30002, "图表生成失败"),

    AI_SERVICE_ERROR(40001, "AI服务调用失败"),
    AI_RATE_LIMIT(40002, "AI调用次数已达上限，请稍后再试"),
    AI_RESPONSE_PARSE_ERROR(40003, "AI响应解析失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
