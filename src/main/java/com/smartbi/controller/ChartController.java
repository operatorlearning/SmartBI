package com.smartbi.controller;

import com.smartbi.annotation.OperLog;
import com.smartbi.annotation.RateLimit;
import com.smartbi.common.PageResult;
import com.smartbi.common.Result;
import com.smartbi.dto.ChartGenDTO;
import com.smartbi.service.ChartService;
import com.smartbi.vo.ChartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 图表接口 — AI 智能图表生成与管理
 */
@Tag(name = "智能图表")
@RestController
@RequestMapping("/chart")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    @PostMapping("/gen/sync")
    @Operation(summary = "同步生成图表（等待AI返回结果）")
    @OperLog("同步生成图表")
    @RateLimit(key = "ai_gen", window = 60, maxCount = 5, message = "AI调用过于频繁，每分钟最多5次")
    public Result<ChartVO> generateSync(@RequestBody @Valid ChartGenDTO dto) {
        return Result.success(chartService.generateChartSync(dto));
    }

    @PostMapping("/gen/async")
    @Operation(summary = "异步生成图表（MQ异步处理，通过SSE查询进度）")
    @OperLog("异步生成图表")
    @RateLimit(key = "ai_gen", window = 60, maxCount = 10, message = "AI调用过于频繁，请稍后再试")
    public Result<ChartVO> generateAsync(@RequestBody @Valid ChartGenDTO dto) {
        return Result.success(chartService.generateChartAsync(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询我的图表")
    public Result<PageResult<ChartVO>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name) {
        return Result.success(chartService.listMyCharts(current, pageSize, name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取图表详情")
    public Result<ChartVO> detail(@PathVariable Long id) {
        return Result.success(chartService.getChartDetail(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除图表")
    @OperLog("删除图表")
    public Result<Void> delete(@PathVariable Long id) {
        chartService.deleteChart(id);
        return Result.success();
    }
}
