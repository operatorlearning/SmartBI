package com.smartbi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图表实体
 */
@Data
@TableName("chart")
public class Chart implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long datasetId;

    private String name;

    /** 分析目标 */
    private String goal;

    /** 图表类型: bar/line/pie/radar/scatter */
    private String chartType;

    /** ECharts option JSON */
    private String chartOption;

    /** AI分析结论 */
    private String analysisResult;

    /** 原始数据(CSV) */
    private String rawData;

    /** 状态: wait/running/succeed/failed */
    private String status;

    /** 执行信息 */
    private String execMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
