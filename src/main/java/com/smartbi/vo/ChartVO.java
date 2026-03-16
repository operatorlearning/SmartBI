package com.smartbi.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图表信息响应
 */
@Data
public class ChartVO implements Serializable {

    private Long id;
    private String name;
    private String goal;
    private String chartType;
    private String chartOption;
    private String analysisResult;
    private String rawData;
    private String status;
    private String execMessage;
    private Long datasetId;
    private LocalDateTime createTime;
}
