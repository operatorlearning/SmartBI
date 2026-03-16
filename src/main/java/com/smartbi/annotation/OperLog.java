package com.smartbi.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 — AOP 自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 操作描述 */
    String value() default "";
}
