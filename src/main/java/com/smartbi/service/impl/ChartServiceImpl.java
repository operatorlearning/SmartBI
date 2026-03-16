package com.smartbi.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartbi.common.BusinessException;
import com.smartbi.common.ErrorCode;
import com.smartbi.common.PageResult;
import com.smartbi.dto.ChartGenDTO;
import com.smartbi.entity.AiTask;
import com.smartbi.entity.Chart;
import com.smartbi.entity.Dataset;
import com.smartbi.mapper.AiTaskMapper;
import com.smartbi.mapper.ChartMapper;
import com.smartbi.mq.AiMessageProducer;
import com.smartbi.service.AiService;
import com.smartbi.service.ChartService;
import com.smartbi.service.DatasetService;
import com.smartbi.service.UserService;
import com.smartbi.vo.ChartVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图表服务实现
 * <p>
 * 核心亮点：
 * 1. 同步/异步双模式生成
 * 2. AI 响应解析 → ECharts option 存储
 * 3. MQ 异步任务 + 状态跟踪
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    private final UserService userService;
    private final DatasetService datasetService;
    private final AiService aiService;
    private final AiTaskMapper aiTaskMapper;
    private final AiMessageProducer aiMessageProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChartVO generateChartSync(ChartGenDTO dto) {
        Long userId = userService.getCurrentUserId();

        // 1. 获取原始数据
        String csvData = resolveCsvData(dto);

        // 2. 创建图表记录（状态: running）
        Chart chart = buildChart(dto, userId, csvData);
        chart.setStatus("running");
        save(chart);

        // 3. 调用 AI 服务
        try {
            String aiResponse = aiService.doChat(csvData, dto.getGoal(), dto.getChartType());
            parseAndUpdateChart(chart, aiResponse);
            chart.setStatus("succeed");
            updateById(chart);
            log.info("同步图表生成成功: chartId={}", chart.getId());
        } catch (Exception e) {
            chart.setStatus("failed");
            chart.setExecMessage("AI分析失败: " + e.getMessage());
            updateById(chart);
            log.error("同步图表生成失败: chartId={}, error={}", chart.getId(), e.getMessage());
            throw new BusinessException(ErrorCode.CHART_GEN_ERROR, "图表生成失败: " + e.getMessage());
        }

        ChartVO vo = new ChartVO();
        BeanUtils.copyProperties(chart, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChartVO generateChartAsync(ChartGenDTO dto) {
        Long userId = userService.getCurrentUserId();

        // 1. 获取原始数据
        String csvData = resolveCsvData(dto);

        // 2. 创建图表记录（状态: wait）
        Chart chart = buildChart(dto, userId, csvData);
        chart.setStatus("wait");
        save(chart);

        // 3. 创建 AI 任务记录
        AiTask aiTask = new AiTask();
        aiTask.setChartId(chart.getId());
        aiTask.setUserId(userId);
        aiTask.setStatus("pending");
        aiTask.setProgress(0);
        aiTaskMapper.insert(aiTask);

        // 4. 发送 MQ 消息（异步处理）
        aiMessageProducer.sendAiGenMessage(chart.getId());
        log.info("异步图表任务已提交: chartId={}, taskId={}", chart.getId(), aiTask.getId());

        ChartVO vo = new ChartVO();
        BeanUtils.copyProperties(chart, vo);
        return vo;
    }

    @Override
    public PageResult<ChartVO> listMyCharts(int current, int pageSize, String name) {
        Long userId = userService.getCurrentUserId();

        LambdaQueryWrapper<Chart> wrapper = new LambdaQueryWrapper<Chart>()
                .eq(Chart::getUserId, userId)
                .like(StringUtils.hasText(name), Chart::getName, name)
                .orderByDesc(Chart::getCreateTime);

        Page<Chart> page = page(new Page<>(current, pageSize), wrapper);

        Page<ChartVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ChartVO> voList = page.getRecords().stream().map(c -> {
            ChartVO vo = new ChartVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).toList();
        voPage.setRecords(voList);

        return PageResult.of(voPage);
    }

    @Override
    public ChartVO getChartDetail(Long id) {
        Chart chart = getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.CHART_NOT_FOUND);
        }
        Long userId = userService.getCurrentUserId();
        if (!chart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        ChartVO vo = new ChartVO();
        BeanUtils.copyProperties(chart, vo);
        return vo;
    }

    @Override
    public void deleteChart(Long id) {
        Chart chart = getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.CHART_NOT_FOUND);
        }
        Long userId = userService.getCurrentUserId();
        if (!chart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        removeById(id);
        log.info("图表删除: id={}", id);
    }

    // ==================== 私有方法 ====================

    /**
     * 解析 CSV 数据（从数据集或直接传入）
     */
    private String resolveCsvData(ChartGenDTO dto) {
        if (dto.getDatasetId() != null) {
            Dataset dataset = datasetService.getById(dto.getDatasetId());
            if (dataset == null) {
                throw new BusinessException(ErrorCode.DATASET_NOT_FOUND);
            }
            return dataset.getRawData();
        }
        if (StringUtils.hasText(dto.getRawData())) {
            return dto.getRawData();
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "请提供数据集ID或原始数据");
    }

    /**
     * 构建 Chart 实体
     */
    private Chart buildChart(ChartGenDTO dto, Long userId, String csvData) {
        Chart chart = new Chart();
        chart.setUserId(userId);
        chart.setDatasetId(dto.getDatasetId());
        chart.setName(StringUtils.hasText(dto.getName()) ? dto.getName() : "AI分析图表");
        chart.setGoal(dto.getGoal());
        chart.setChartType(dto.getChartType());
        chart.setRawData(csvData);
        return chart;
    }

    /**
     * 解析 AI 响应并更新图表
     */
    public void parseAndUpdateChart(Chart chart, String aiResponse) {
        try {
            // AI 响应可能包含 markdown 代码块标记，需清理
            String cleaned = aiResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            JSONObject json = JSONUtil.parseObj(cleaned);
            chart.setChartType(json.getStr("chartType", chart.getChartType()));
            chart.setChartOption(json.getStr("option", json.getJSONObject("option") != null
                    ? json.getJSONObject("option").toString() : null));

            // option 可能是对象或字符串
            Object optionObj = json.get("option");
            if (optionObj instanceof JSONObject) {
                chart.setChartOption(optionObj.toString());
            } else if (optionObj instanceof String) {
                chart.setChartOption((String) optionObj);
            }

            chart.setAnalysisResult(json.getStr("analysis"));

            if (json.containsKey("chartName") && !StringUtils.hasText(chart.getName())) {
                chart.setName(json.getStr("chartName"));
            }
        } catch (Exception e) {
            log.error("AI响应解析失败: response={}", aiResponse, e);
            // 保存原始响应供调试
            chart.setAnalysisResult(aiResponse);
            throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_ERROR);
        }
    }
}
