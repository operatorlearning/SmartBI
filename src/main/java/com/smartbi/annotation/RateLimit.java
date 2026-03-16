package com.smartbi.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解 — 基于 Redis Lua 滑动窗口计数器
 * <p>
 * 支持用户级别限流，防止单用户频繁调用 AI 接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 限流 key 前缀 */
    String key() default "rate_limit";

    /** 时间窗口（秒） */
    int window() default 60;

    /** 窗口内最大请求次数 */
    int maxCount() default 5;

    /** 限流提示信息 */
    String message() default "请求过于频繁，请稍后再试";
}
