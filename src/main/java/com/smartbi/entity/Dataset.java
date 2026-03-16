package com.smartbi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据集实体
 */
@Data
@TableName("dataset")
public class Dataset implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String description;

    private String fileName;

    /** 列信息 JSON: [{name, type}] */
    private String columnsInfo;

    private Integer rowCount;

    /** 原始数据(CSV格式) */
    private String rawData;

    /** 文件大小(bytes) */
    private Long size;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
