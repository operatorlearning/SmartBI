package com.smartbi.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rabbitmq.client.Channel;
import com.smartbi.config.RabbitMQConfig;
import com.smartbi.entity.AiTask;
import com.smartbi.entity.Chart;
import com.smartbi.mapper.AiTaskMapper;
import com.smartbi.mapper.ChartMapper;
import com.smartbi.service.AiService;
import com.smartbi.service.impl.ChartServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * AI 消息消费者 — 异步处理图表生成任务
 * <p>
 * 技术亮点：
 * 1. 手动 ACK 确保消息可靠消费
 * 2. 任务状态跟踪 + 进度更新
 * 3. 失败自动进入死信队列
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiMessageConsumer {

    private final ChartMapper chartMapper;
    private final AiTaskMapper aiTaskMapper;
    private final AiService aiService;
    private final ChartServiceImpl chartService;

    @RabbitListener(queues = RabbitMQConfig.AI_GEN_QUEUE, ackMode = "MANUAL")
    public void handleAiGenMessage(Long chartId, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("收到AI生成任务: chartId={}, deliveryTag={}", chartId, deliveryTag);

        try {
            // 1. 查询图表
            Chart chart = chartMapper.selectById(chartId);
            if (chart == null) {
                log.warn("图表不存在, 丢弃消息: chartId={}", chartId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. 更新任务状态为 running
            updateTaskStatus(chartId, "running", 10, null);
            chart.setStatus("running");
            chartMapper.updateById(chart);

            // 3. 调用 AI 服务
            updateTaskStatus(chartId, "running", 30, null);
            String aiResponse = aiService.doChat(chart.getRawData(), chart.getGoal(), chart.getChartType());

            // 4. 解析 AI 响应
            updateTaskStatus(chartId, "running", 70, null);
            chartService.parseAndUpdateChart(chart, aiResponse);

            // 5. 更新图表状态为 succeed
            chart.setStatus("succeed");
            chartMapper.updateById(chart);
            updateTaskStatus(chartId, "succeed", 100, null);

            log.info("异步图表生成成功: chartId={}", chartId);

            // 6. 手动 ACK
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("异步图表生成失败: chartId={}, error={}", chartId, e.getMessage(), e);

            // 更新失败状态
            Chart failedChart = new Chart();
            failedChart.setId(chartId);
            failedChart.setStatus("failed");
            failedChart.setExecMessage("AI分析失败: " + e.getMessage());
            chartMapper.updateById(failedChart);
            updateTaskStatus(chartId, "failed", 0, e.getMessage());

            // NACK 不重新入队（已有死信队列兜底）
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * 更新 AI 任务状态（用于 SSE 进度推送查询）
     */
    private void updateTaskStatus(Long chartId, String status, int progress, String errorMsg) {
        LambdaUpdateWrapper<AiTask> wrapper = new LambdaUpdateWrapper<AiTask>()
                .eq(AiTask::getChartId, chartId)
                .set(AiTask::getStatus, status)
                .set(AiTask::getProgress, progress);

        if ("running".equals(status) && progress == 10) {
            wrapper.set(AiTask::getStartTime, LocalDateTime.now());
        }
        if ("succeed".equals(status) || "failed".equals(status)) {
            wrapper.set(AiTask::getEndTime, LocalDateTime.now());
        }
        if (errorMsg != null) {
            wrapper.set(AiTask::getErrorMsg, errorMsg);
        }

        aiTaskMapper.update(null, wrapper);
    }
}
