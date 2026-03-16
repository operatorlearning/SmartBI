package com.smartbi.aspect;

import com.smartbi.annotation.RateLimit;
import com.smartbi.common.BusinessException;
import com.smartbi.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 限流切面 — Redis Lua 滑动窗口计数器
 * <p>
 * 技术亮点：
 * 1. Lua 脚本保证原子性（ZADD + ZREMRANGEBYSCORE + ZCARD）
 * 2. 支持用户级别限流（key 与用户绑定）
 * 3. 滑动窗口比固定窗口更平滑
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Lua 脚本：滑动窗口限流
     * KEYS[1] = 限流 key
     * ARGV[1] = 窗口大小(ms)
     * ARGV[2] = 最大请求数
     * ARGV[3] = 当前时间戳(ms)
     * ARGV[4] = 唯一请求ID
     * 返回: 1=允许, 0=拒绝
     */
    private static final String SLIDING_WINDOW_LUA =
            """
            local key = KEYS[1]
            local window = tonumber(ARGV[1])
            local maxCount = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local uid = ARGV[4]
            local min = now - window
            redis.call('ZREMRANGEBYSCORE', key, 0, min)
            local count = redis.call('ZCARD', key)
            if count < maxCount then
                redis.call('ZADD', key, now, uid)
                redis.call('PEXPIRE', key, window)
                return 1
            end
            return 0
            """;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 获取当前用户
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        String key = "smartbi:rate_limit:" + rateLimit.key() + ":" + username;
        long windowMs = rateLimit.window() * 1000L;
        long now = System.currentTimeMillis();
        String uid = now + "-" + Thread.currentThread().getId();

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SLIDING_WINDOW_LUA, Long.class);
        Long result = stringRedisTemplate.execute(script,
                Collections.singletonList(key),
                String.valueOf(windowMs),
                String.valueOf(rateLimit.maxCount()),
                String.valueOf(now),
                uid);

        if (result == null || result == 0L) {
            log.warn("限流触发: user={}, key={}", username, key);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, rateLimit.message());
        }

        return point.proceed();
    }
}
