package com.smartbi.aspect;

import cn.hutool.json.JSONUtil;
import com.smartbi.annotation.OperLog;
import com.smartbi.entity.OperationLog;
import com.smartbi.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面 — AOP 自动记录接口操作
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = point.proceed();
        long duration = System.currentTimeMillis() - startTime;

        try {
            saveLog(point, operLog, duration);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage());
        }

        return result;
    }

    private void saveLog(ProceedingJoinPoint point, OperLog operLog, long duration) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String method = signature.getDeclaringTypeName() + "." + signature.getName();

        OperationLog logEntity = new OperationLog();
        logEntity.setOperation(operLog.value());
        logEntity.setMethod(method);
        logEntity.setDuration(duration);

        // 请求参数（截断防止过长）
        try {
            String params = JSONUtil.toJsonStr(point.getArgs());
            logEntity.setParams(params.length() > 2000 ? params.substring(0, 2000) : params);
        } catch (Exception e) {
            logEntity.setParams("参数序列化失败");
        }

        // 当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            logEntity.setUsername(auth.getName());
        }

        // 请求IP
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            logEntity.setIp(getClientIp(request));
        }

        operationLogMapper.insert(logEntity);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
