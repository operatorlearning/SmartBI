package com.smartbi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * SmartBI 智能BI数据分析平台 — 启动类
 */
@SpringBootApplication
@MapperScan("com.smartbi.mapper")
@EnableAsync
public class SmartBiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBiApplication.class, args);
    }
}
