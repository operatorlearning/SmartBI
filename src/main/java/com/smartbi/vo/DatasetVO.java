package com.smartbi.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据集信息响应
 */
@Data
public class DatasetVO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private String fileName;
    private Integer rowCount;
    private Long size;
    private String columnsInfo;
    private LocalDateTime createTime;
}
