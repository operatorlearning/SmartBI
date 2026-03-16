package com.smartbi.service;

/**
 * AI 服务接口 — 调用 AI API 生成图表分析
 * <p>
 * 策略模式：支持真实 AI API 调用和 Mock 模式
 */
public interface AiService {

    /**
     * 调用 AI 生成图表配置和分析结论
     *
     * @param csvData 原始数据（CSV格式）
     * @param goal    分析目标
     * @param chartType 指定图表类型（可为null，AI自动选择）
     * @return AI 响应（JSON字符串，包含 chartType / option / analysis）
     */
    String doChat(String csvData, String goal, String chartType);
}
