package com.smartbi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置 — AI任务异步处理专用线程池
 * <p>
 * 亮点：线程池隔离，AI任务不影响主业务线程
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolConfig {

    private int coreSize = 4;
    private int maxSize = 8;
    private int queueCapacity = 100;
    private int keepAlive = 60;
    private String namePrefix = "ai-task-";

    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAlive);
        executor.setThreadNamePrefix(namePrefix);
        // CallerRunsPolicy: 线程池满时由调用线程执行，防止任务丢弃
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
