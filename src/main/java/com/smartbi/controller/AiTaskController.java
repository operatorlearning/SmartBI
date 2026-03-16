package com.smartbi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartbi.common.Result;
import com.smartbi.entity.AiTask;
import com.smartbi.mapper.AiTaskMapper;
import com.smartbi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * AI 任务接口 — SSE 实时进度推送
 * <p>
 * 技术亮点：
 * 1. SSE（Server-Sent Events）长连接推送任务进度
 * 2. 轮询检测 + 自动断开，资源自动回收
 * 3. 前端 EventSource 实时接收进度更新
 */
@Slf4j
@Tag(name = "AI任务")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiTaskController {

    private final AiTaskMapper aiTaskMapper;
    private final UserService userService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @GetMapping("/task/{chartId}")
    @Operation(summary = "查询AI任务状态")
    public Result<AiTask> getTaskStatus(@PathVariable Long chartId) {
        AiTask task = aiTaskMapper.selectOne(
                new LambdaQueryWrapper<AiTask>().eq(AiTask::getChartId, chartId));
        return Result.success(task);
    }

    @GetMapping(value = "/sse/{chartId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE实时推送AI任务进度")
    public SseEmitter sseProgress(@PathVariable Long chartId) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2分钟超时

        // 定时轮询任务状态并推送
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                AiTask task = aiTaskMapper.selectOne(
                        new LambdaQueryWrapper<AiTask>().eq(AiTask::getChartId, chartId));
                if (task == null) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("message", "任务不存在")));
                    emitter.complete();
                    return;
                }

                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(Map.of(
                                "status", task.getStatus(),
                                "progress", task.getProgress(),
                                "errorMsg", task.getErrorMsg() != null ? task.getErrorMsg() : ""
                        )));

                // 任务完成或失败，关闭 SSE
                if ("succeed".equals(task.getStatus()) || "failed".equals(task.getStatus())) {
                    emitter.complete();
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, 0, 2, TimeUnit.SECONDS);

        // 连接关闭时取消轮询
        emitter.onCompletion(() -> future.cancel(true));
        emitter.onTimeout(() -> future.cancel(true));
        emitter.onError(e -> future.cancel(true));

        return emitter;
    }
}
