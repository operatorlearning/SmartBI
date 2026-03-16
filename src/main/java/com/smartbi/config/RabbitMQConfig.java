package com.smartbi.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * <p>
 * 交换机/队列架构：
 * ai.direct (直连交换机) → ai.gen.queue (AI图表生成队列)
 * ai.dlx.exchange (死信交换机) → ai.dlx.queue (失败任务队列)
 */
@Configuration
public class RabbitMQConfig {

    // ==================== AI 图表生成 ====================
    public static final String AI_EXCHANGE = "ai.direct";
    public static final String AI_GEN_QUEUE = "ai.gen.queue";
    public static final String AI_GEN_ROUTING_KEY = "ai.gen";

    // ==================== 死信队列（失败重试耗尽后） ====================
    public static final String AI_DLX_EXCHANGE = "ai.dlx.exchange";
    public static final String AI_DLX_QUEUE = "ai.dlx.queue";
    public static final String AI_DLX_ROUTING_KEY = "ai.dlx";

    // ==================== 消息转换器 ====================
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // ==================== AI 生成交换机 & 队列 ====================
    @Bean
    public DirectExchange aiExchange() {
        return new DirectExchange(AI_EXCHANGE, true, false);
    }

    @Bean
    public Queue aiGenQueue() {
        return QueueBuilder.durable(AI_GEN_QUEUE)
                .deadLetterExchange(AI_DLX_EXCHANGE)
                .deadLetterRoutingKey(AI_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding aiGenBinding() {
        return BindingBuilder.bind(aiGenQueue()).to(aiExchange()).with(AI_GEN_ROUTING_KEY);
    }

    // ==================== 死信交换机 & 队列 ====================
    @Bean
    public DirectExchange aiDlxExchange() {
        return new DirectExchange(AI_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue aiDlxQueue() {
        return QueueBuilder.durable(AI_DLX_QUEUE).build();
    }

    @Bean
    public Binding aiDlxBinding() {
        return BindingBuilder.bind(aiDlxQueue()).to(aiDlxExchange()).with(AI_DLX_ROUTING_KEY);
    }
}
