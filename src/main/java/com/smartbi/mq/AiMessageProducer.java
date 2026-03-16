package com.smartbi.mq;

import com.smartbi.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * AI 消息生产者 — 将图表生成任务发送到 MQ 队列
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送 AI 图表生成任务消息
     *
     * @param chartId 图表ID
     */
    public void sendAiGenMessage(Long chartId) {
        log.info("发送AI生成任务消息: chartId={}", chartId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.AI_EXCHANGE,
                RabbitMQConfig.AI_GEN_ROUTING_KEY,
                chartId);
    }
}
