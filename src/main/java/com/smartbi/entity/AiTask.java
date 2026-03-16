package com.smartbi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI分析任务实体
 */
@Data
@TableName("ai_task")
public class AiTask implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long chartId;

    private Long userId;

    /** 状态: pending/running/succeed/failed */
    private String status;

    /** 进度 0-100 */
    private Integer progress;

    private String errorMsg;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
