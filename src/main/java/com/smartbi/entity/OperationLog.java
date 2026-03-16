package com.smartbi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    /** 操作描述 */
    private String operation;

    /** 请求方法 */
    private String method;

    /** 请求参数 */
    private String params;

    private String ip;

    /** 耗时(ms) */
    private Long duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
