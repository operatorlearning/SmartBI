package com.smartbi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据集查询请求
 */
@Data
public class DatasetQueryDTO implements Serializable {

    private String name;

    private Integer current = 1;

    private Integer pageSize = 10;
}
