-- =============================================
-- SmartBI 智能BI数据分析平台 - 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS smart_bi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_bi;

-- -----------------------------------------
-- 1. 用户表
-- -----------------------------------------
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
    `email`       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `avatar`      VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
    `role`        VARCHAR(32)  NOT NULL DEFAULT 'user' COMMENT '角色: admin/user',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------
-- 2. 数据集表
-- -----------------------------------------
CREATE TABLE `dataset` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
    `user_id`      BIGINT       NOT NULL COMMENT '所属用户ID',
    `name`         VARCHAR(128) NOT NULL COMMENT '数据集名称',
    `description`  VARCHAR(512) DEFAULT NULL COMMENT '数据集描述',
    `file_name`    VARCHAR(256) DEFAULT NULL COMMENT '原始文件名',
    `columns_info` TEXT         DEFAULT NULL COMMENT '列信息(JSON: [{name,type}])',
    `row_count`    INT          NOT NULL DEFAULT 0 COMMENT '数据行数',
    `raw_data`     MEDIUMTEXT   DEFAULT NULL COMMENT '原始数据(CSV格式)',
    `size`         BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小(bytes)',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0无效 1正常',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集表';

-- -----------------------------------------
-- 3. 图表表
-- -----------------------------------------
CREATE TABLE `chart` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '图表ID',
    `user_id`         BIGINT       NOT NULL COMMENT '所属用户ID',
    `dataset_id`      BIGINT       DEFAULT NULL COMMENT '关联数据集ID',
    `name`            VARCHAR(128) NOT NULL COMMENT '图表名称',
    `goal`            VARCHAR(512) DEFAULT NULL COMMENT '分析目标',
    `chart_type`      VARCHAR(32)  DEFAULT NULL COMMENT '图表类型: bar/line/pie/radar/scatter',
    `chart_option`    MEDIUMTEXT   DEFAULT NULL COMMENT 'ECharts option(JSON)',
    `analysis_result` TEXT         DEFAULT NULL COMMENT 'AI分析结论',
    `raw_data`        MEDIUMTEXT   DEFAULT NULL COMMENT '图表原始数据(CSV)',
    `status`          VARCHAR(16)  NOT NULL DEFAULT 'wait' COMMENT '状态: wait/running/succeed/failed',
    `exec_message`    VARCHAR(512) DEFAULT NULL COMMENT '执行信息',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_dataset_id` (`dataset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图表表';

-- -----------------------------------------
-- 4. AI分析任务表
-- -----------------------------------------
CREATE TABLE `ai_task` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `chart_id`    BIGINT       NOT NULL COMMENT '关联图表ID',
    `user_id`     BIGINT       NOT NULL COMMENT '所属用户ID',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'pending' COMMENT '状态: pending/running/succeed/failed',
    `progress`    INT          NOT NULL DEFAULT 0 COMMENT '进度(0-100)',
    `error_msg`   VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
    `start_time`  DATETIME     DEFAULT NULL COMMENT '开始时间',
    `end_time`    DATETIME     DEFAULT NULL COMMENT '结束时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_chart_id` (`chart_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI分析任务表';

-- -----------------------------------------
-- 5. 操作日志表
-- -----------------------------------------
CREATE TABLE `operation_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`     BIGINT       DEFAULT NULL COMMENT '操作用户ID',
    `username`    VARCHAR(64)  DEFAULT NULL COMMENT '操作用户名',
    `operation`   VARCHAR(64)  DEFAULT NULL COMMENT '操作描述',
    `method`      VARCHAR(256) DEFAULT NULL COMMENT '请求方法',
    `params`      TEXT         DEFAULT NULL COMMENT '请求参数',
    `ip`          VARCHAR(64)  DEFAULT NULL COMMENT 'IP地址',
    `duration`    BIGINT       DEFAULT NULL COMMENT '耗时(ms)',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- -----------------------------------------
-- 初始数据：管理员账号 (admin / admin123)
-- -----------------------------------------
INSERT INTO `user` (`username`, `password`, `role`) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'admin'),
('demo',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'user');
