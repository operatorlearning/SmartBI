package com.smartbi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartbi.common.PageResult;
import com.smartbi.dto.ChartGenDTO;
import com.smartbi.entity.Chart;
import com.smartbi.vo.ChartVO;

/**
 * 图表服务接口
 */
public interface ChartService extends IService<Chart> {

    /**
     * 同步生成图表（AI分析）
     */
    ChartVO generateChartSync(ChartGenDTO dto);

    /**
     * 异步生成图表（提交MQ异步处理）
     */
    ChartVO generateChartAsync(ChartGenDTO dto);

    /**
     * 分页查询当前用户的图表
     */
    PageResult<ChartVO> listMyCharts(int current, int pageSize, String name);

    /**
     * 获取图表详情
     */
    ChartVO getChartDetail(Long id);

    /**
     * 删除图表
     */
    void deleteChart(Long id);
}
