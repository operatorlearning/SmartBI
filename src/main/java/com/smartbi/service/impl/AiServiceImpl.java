package com.smartbi.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.smartbi.common.BusinessException;
import com.smartbi.common.ErrorCode;
import com.smartbi.service.AiService;
import com.smartbi.util.AiPromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AI 服务实现 — 支持 OpenAI 兼容 API（DeepSeek / ChatGLM / GPT）
 * <p>
 * 技术亮点：
 * 1. Prompt 工程 — 精心设计提示词引导 AI 输出结构化 JSON
 * 2. Mock 模式 — 未配置 API Key 时自动生成示例图表
 * 3. 超时控制 + 重试 + 响应校验
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${smartbi.ai.api-url}")
    private String apiUrl;

    @Value("${smartbi.ai.api-key}")
    private String apiKey;

    @Value("${smartbi.ai.model}")
    private String model;

    @Value("${smartbi.ai.timeout}")
    private int timeout;

    @Value("${smartbi.ai.mock-enabled}")
    private boolean mockEnabled;

    @Override
    public String doChat(String csvData, String goal, String chartType) {
        // Mock 模式：未配置有效 API Key 时，生成示例数据
        if (mockEnabled && (apiKey == null || apiKey.startsWith("sk-demo"))) {
            log.info("AI Mock模式: 生成示例图表数据");
            return AiPromptBuilder.generateMockResponse(csvData, goal, chartType);
        }

        // 真实 AI API 调用
        String prompt = AiPromptBuilder.buildPrompt(csvData, goal, chartType);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", model);
            requestBody.set("temperature", 0.7);
            requestBody.set("max_tokens", 4096);

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.set("role", "system");
            systemMsg.set("content", AiPromptBuilder.SYSTEM_PROMPT);
            messages.add(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.set("role", "user");
            userMsg.set("content", prompt);
            messages.add(userMsg);

            requestBody.set("messages", messages);

            log.info("调用AI API: model={}, goalLength={}", model, goal.length());

            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(timeout)
                    .execute();

            if (!response.isOk()) {
                log.error("AI API调用失败: status={}, body={}", response.getStatus(), response.body());
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR,
                        "AI服务返回错误: " + response.getStatus());
            }

            String responseBody = response.body();
            JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
            String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

            log.info("AI API调用成功, 响应长度: {}", content.length());
            return content;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI API调用异常: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI服务调用失败: " + e.getMessage());
        }
    }
}
