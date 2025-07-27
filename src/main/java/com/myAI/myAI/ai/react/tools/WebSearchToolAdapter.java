package com.myAI.myAI.ai.react.tools;

import com.myAI.myAI.ai.react.ReactTool;
import com.myAI.myAI.langchain4j.tools.WebSearchTool;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 网络搜索工具适配器
 * 将LangChain4j的WebSearchTool适配到ReAct系统
 */
@Component
@Slf4j
public class WebSearchToolAdapter implements ReactTool {
    
    private final WebSearchTool webSearchTool;
    
    public WebSearchToolAdapter() {
        this.webSearchTool = new WebSearchTool();
    }
    
    @Override
    public String getName() {
        return "WEB_SEARCH_ENHANCED";
    }
    
    @Override
    public String getDescription() {
        return "增强版网络搜索工具，使用百度搜索引擎获取最新信息";
    }
    
    @Override
    public String execute(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return "错误：请提供搜索关键词";
            }
            
            String query = input.trim();
            log.info("执行网络搜索，关键词: {}", query);
            
            String result = webSearchTool.searchWeb(query);
            
            if (result == null || result.startsWith("Error searching")) {
                return "搜索失败，请检查网络连接或稍后重试";
            }
            
            // 格式化搜索结果
            return formatSearchResult(query, result);
            
        } catch (Exception e) {
            log.error("网络搜索失败", e);
            return "网络搜索失败: " + e.getMessage();
        }
    }
    
    @Override
    public String getParameterDescription() {
        return "搜索关键词，支持中文和英文\n" +
               "示例: '人工智能发展趋势', 'ChatGPT最新消息', '北京天气'\n" +
               "提示: 使用具体的关键词可以获得更准确的搜索结果";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    /**
     * 格式化搜索结果
     */
    private String formatSearchResult(String query, String rawResult) {
        try {
            StringBuilder formattedResult = new StringBuilder();
            formattedResult.append("🔍 搜索关键词: ").append(query).append("\n\n");
            
            // 尝试解析JSON格式的搜索结果
            if (rawResult.startsWith("[") || rawResult.startsWith("{")) {
                return formatJsonSearchResult(formattedResult, rawResult);
            } else {
                // 如果不是JSON格式，直接返回
                formattedResult.append("搜索结果:\n").append(rawResult);
                return formattedResult.toString();
            }
            
        } catch (Exception e) {
            log.warn("搜索结果格式化失败", e);
            return "🔍 搜索关键词: " + query + "\n\n搜索结果:\n" + rawResult;
        }
    }
    
    /**
     * 格式化JSON格式的搜索结果
     */
    private String formatJsonSearchResult(StringBuilder formattedResult, String rawResult) {
        try {
            // 处理多个JSON对象用逗号分隔的情况
            String[] jsonObjects = rawResult.split("(?<=})\\s*,\\s*(?=\\{)");
            
            formattedResult.append("📋 搜索结果 (前").append(jsonObjects.length).append("条):\n\n");
            
            for (int i = 0; i < jsonObjects.length && i < 5; i++) {
                try {
                    JSONObject item = JSONUtil.parseObj(jsonObjects[i]);
                    
                    formattedResult.append("【").append(i + 1).append("】");
                    
                    // 提取标题
                    if (item.containsKey("title")) {
                        formattedResult.append(" ").append(item.getStr("title")).append("\n");
                    }
                    
                    // 提取链接
                    if (item.containsKey("link")) {
                        formattedResult.append("🔗 ").append(item.getStr("link")).append("\n");
                    }
                    
                    // 提取摘要
                    if (item.containsKey("snippet")) {
                        String snippet = item.getStr("snippet");
                        if (snippet != null && snippet.length() > 100) {
                            snippet = snippet.substring(0, 100) + "...";
                        }
                        formattedResult.append("📝 ").append(snippet).append("\n");
                    }
                    
                    formattedResult.append("\n");
                    
                } catch (Exception e) {
                    log.warn("解析第{}个搜索结果失败", i + 1, e);
                    formattedResult.append("【").append(i + 1).append("】解析失败\n\n");
                }
            }
            
            return formattedResult.toString();
            
        } catch (Exception e) {
            log.warn("JSON搜索结果格式化失败", e);
            formattedResult.append("搜索结果:\n").append(rawResult);
            return formattedResult.toString();
        }
    }
}
