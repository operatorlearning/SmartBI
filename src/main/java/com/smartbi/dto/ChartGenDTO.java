package com.smartbi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * AI图表生成请求
 */
@Data
public class ChartGenDTO implements Serializable {

    /** 数据集ID（与 rawData 二选一） */
    private Long datasetId;

    /** 直接传入的CSV数据 */
    private String rawData;

    @NotBlank(message = "分析目标不能为空")
    private String goal;

    /** 图表名称（可选，AI自动生成） */
    private String name;

    /** 指定图表类型（可选，AI自动选择） */
    private String chartType;

    /** 是否异步生成 */
    private Boolean async = false;
}
