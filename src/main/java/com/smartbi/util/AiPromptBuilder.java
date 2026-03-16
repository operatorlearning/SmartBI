package com.smartbi.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.*;

/**
 * AI Prompt 构建器 — Prompt 工程核心
 * <p>
 * 技术亮点：
 * 1. 精心设计 System Prompt 约束 AI 输出格式
 * 2. 结构化 JSON 输出约定（chartType / option / analysis）
 * 3. 支持 Mock 模式生成示例图表
 */
public class AiPromptBuilder {

    /**
     * System Prompt — 约束 AI 行为和输出格式
     */
    public static final String SYSTEM_PROMPT = """
            你是一个专业的数据分析师和可视化专家。
            用户会给你数据（CSV格式）和分析目标，你需要:
            1. 分析数据并选择最合适的图表类型
            2. 生成ECharts图表配置（option）
            3. 给出专业的分析结论
            
            你必须严格按照以下JSON格式返回，不要有任何多余内容:
            ```json
            {
              "chartType": "bar|line|pie|radar|scatter",
              "chartName": "图表标题",
              "option": {
                // 完整的ECharts option配置，直接可用
              },
              "analysis": "数据分析结论，包含关键发现和建议（200字以内）"
            }
            ```
            
            注意事项:
            - option 必须是合法的 ECharts 配置对象
            - 图表要美观，配色协调，包含legend、tooltip
            - 分析结论要言之有物，指出关键趋势和异常
            - 如果用户指定了图表类型，请使用指定类型
            """;

    /**
     * 构建用户 Prompt
     */
    public static String buildPrompt(String csvData, String goal, String chartType) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 分析目标\n").append(goal).append("\n\n");

        if (chartType != null && !chartType.isEmpty()) {
            sb.append("## 指定图表类型\n").append(chartType).append("\n\n");
        }

        sb.append("## 原始数据（CSV格式）\n```\n");
        // 限制发送给 AI 的数据行数（前100行足够分析）
        String[] lines = csvData.split("\n");
        int maxLines = Math.min(lines.length, 101); // 表头 + 100行数据
        for (int i = 0; i < maxLines; i++) {
            sb.append(lines[i]).append("\n");
        }
        if (lines.length > 101) {
            sb.append("... 共 ").append(lines.length - 1).append(" 行数据（已截取前100行）\n");
        }
        sb.append("```\n");

        return sb.toString();
    }

    /**
     * Mock 模式 — 基于数据特征生成示例图表
     */
    public static String generateMockResponse(String csvData, String goal, String chartType) {
        String[] lines = csvData.split("\n");
        String[] headers = lines[0].split(",");

        // 根据数据特征自动选择图表类型
        String type = (chartType != null && !chartType.isEmpty()) ? chartType : "bar";
        if (headers.length == 2) {
            type = "pie"; // 两列数据倾向饼图
        }

        // 构建 Mock ECharts Option
        JSONObject option = new JSONObject(new LinkedHashMap<>());
        JSONObject title = new JSONObject();
        // 标题截断，避免过长遮挡图表
        String titleText = goal.length() > 20 ? goal.substring(0, 20) + "..." : goal;
        title.set("text", titleText);
        title.set("left", "left");
        title.set("top", "5");
        JSONObject titleTextStyle = new JSONObject();
        titleTextStyle.set("fontSize", 14);
        titleTextStyle.set("fontWeight", "bold");
        title.set("textStyle", titleTextStyle);
        option.set("title", title);

        JSONObject tooltip = new JSONObject();
        tooltip.set("trigger", "axis");
        option.set("tooltip", tooltip);

        // 全局 grid 留出足够边距，防止文字重叠
        JSONObject grid = new JSONObject();
        grid.set("top", "60");
        grid.set("left", "60");
        grid.set("right", "30");
        grid.set("bottom", "40");
        grid.set("containLabel", true);
        option.set("grid", grid);

        // 解析样本数据
        List<String> categories = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (int i = 1; i < Math.min(lines.length, 8); i++) {
            String[] parts = lines[i].split(",");
            if (parts.length >= 1) categories.add(parts[0].trim());
            if (parts.length >= 2) {
                try {
                    values.add(Double.parseDouble(parts[1].trim().replace("\"", "")));
                } catch (NumberFormatException e) {
                    values.add((double) (i * 100));
                }
            }
        }

        if ("pie".equals(type)) {
            tooltip.set("trigger", "item");
            List<Map<String, Object>> pieData = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", categories.get(i));
                item.put("value", i < values.size() ? values.get(i) : (i + 1) * 100);
                pieData.add(item);
            }
            JSONObject series = new JSONObject();
            series.set("type", "pie");
            series.set("radius", "55%");
            series.set("center", List.of("50%", "55%"));
            series.set("data", pieData);
            // 饼图标签配置
            JSONObject pieLabelStyle = new JSONObject();
            pieLabelStyle.set("show", true);
            pieLabelStyle.set("fontSize", 12);
            pieLabelStyle.set("formatter", "{b}: {d}%");
            series.set("label", pieLabelStyle);
            option.set("series", List.of(series));
        } else {
            JSONObject xAxis = new JSONObject();
            xAxis.set("type", "category");
            xAxis.set("data", categories);
            // x轴标签防重叠
            JSONObject axisLabel = new JSONObject();
            axisLabel.set("rotate", categories.size() > 6 ? 30 : 0);
            axisLabel.set("fontSize", 11);
            xAxis.set("axisLabel", axisLabel);
            option.set("xAxis", xAxis);

            JSONObject yAxis = new JSONObject();
            yAxis.set("type", "value");
            option.set("yAxis", yAxis);

            JSONObject legend = new JSONObject();
            legend.set("data", List.of(headers.length > 1 ? headers[1].trim() : "数值"));
            legend.set("top", "28");
            legend.set("left", "center");
            option.set("legend", legend);

            JSONObject series = new JSONObject();
            series.set("name", headers.length > 1 ? headers[1].trim() : "数值");
            series.set("type", type);
            series.set("data", values.isEmpty()
                    ? List.of(120, 200, 150, 80, 70, 110, 130)
                    : values);
            option.set("series", List.of(series));
        }

        // 组装完整响应
        JSONObject response = new JSONObject(new LinkedHashMap<>());
        response.set("chartType", type);
        response.set("chartName", goal);
        response.set("option", option);
        response.set("analysis",
                String.format("基于对数据的分析（共%d条记录，%d个字段），%s。数据整体呈现明显的分布特征，"
                                + "建议关注高值区间的变化趋势，并结合业务场景进一步深入分析。"
                                + "（Mock模式 — 配置真实AI API Key后将获得精准分析结果）",
                        lines.length - 1, headers.length, goal));

        return response.toString();
    }
}
