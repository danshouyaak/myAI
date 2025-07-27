package com.myAI.myAI.ai.react.tools;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.myAI.myAI.ai.react.ReactTool;
import com.myAI.myAI.config.McpConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * MCP Web搜索工具
 */
@Component
@Slf4j
public class McpWebSearchTool implements ReactTool {

    @Resource
    private McpConfig mcpConfig;

    @Resource
    private Gson gson;

    private final WebClient webClient;

    // 支持的搜索引擎列表
    private static final List<String> SUPPORTED_ENGINES = Arrays.asList(
        "bing", "baidu", "linuxdo", "csdn", "duckduckgo", "exa", "brave"
    );

    public McpWebSearchTool() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public String getName() {
        return "WEB_SEARCH";
    }

    @Override
    public String getDescription() {
        return "使用MCP服务进行网络搜索，支持多个搜索引擎，可以搜索实时的互联网信息";
    }

    @Override
    public String execute(String input) {
        try {
            // 解析输入参数
            SearchRequest searchRequest = parseInput(input);
            String mcpUrl = mcpConfig.getMcpServers().get("web-search").getTransport().getUrl();
            
            // 构建MCP请求
            McpRequest mcpRequest = new McpRequest();
            mcpRequest.setServerName("web-search");
            mcpRequest.setToolName("search");
            mcpRequest.setArguments(searchRequest);

            // 发送请求并等待响应
            String response = webClient.post()
                .uri(mcpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(gson.toJson(mcpRequest))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // 解析响应
            List<SearchResult> results = Arrays.asList(gson.fromJson(response, SearchResult[].class));
            
            // 格式化结果
            StringBuilder formattedResults = new StringBuilder();
            formattedResults.append("搜索结果:\n");
            for (SearchResult result : results) {
                formattedResults.append("---\n");
                formattedResults.append("标题: ").append(result.getTitle()).append("\n");
                formattedResults.append("链接: ").append(result.getUrl()).append("\n");
                formattedResults.append("描述: ").append(result.getDescription()).append("\n");
                formattedResults.append("来源: ").append(result.getSource()).append("\n");
                formattedResults.append("引擎: ").append(result.getEngine()).append("\n");
            }

            return formattedResults.toString();
        } catch (Exception e) {
            log.error("Web search failed", e);
            return "搜索失败: " + e.getMessage();
        }
    }

    @Override
    public String getParameterDescription() {
        return String.format(
            "搜索参数格式: '查询词|结果数量|搜索引擎列表'\n" +
            "- 查询词: 必填，要搜索的内容\n" +
            "- 结果数量: 可选，默认10\n" +
            "- 搜索引擎: 可选，默认bing，支持的引擎：%s\n" +
            "示例: 'AI发展|5|bing,csdn'", 
            String.join(", ", SUPPORTED_ENGINES)
        );
    }

    @Override
    public boolean isAvailable() {
        try {
            Map<String, McpConfig.ServerConfig> mcpServers = mcpConfig.getMcpServers();
            if (mcpServers == null) {
                log.warn("MCP servers configuration is null, web search tool not available");
                return false;
            }
            return mcpServers.containsKey("web-search");
        } catch (Exception e) {
            log.error("MCP web search tool availability check failed", e);
            return false;
        }
    }

    /**
     * 解析输入参数
     */
    private SearchRequest parseInput(String input) {
        SearchRequest request = new SearchRequest();
        String[] parts = input.split("\\|");
        
        // 设置查询词（必填）
        request.setQuery(parts[0].trim());
        
        // 设置结果数量（可选）
        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            try {
                request.setLimit(Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException e) {
                log.warn("Invalid limit format, using default");
            }
        }
        
        // 设置搜索引擎（可选）
        if (parts.length > 2 && !parts[2].trim().isEmpty()) {
            request.setEngines(Arrays.asList(parts[2].trim().split(",")));
        }
        
        return request;
    }

    @Data
    private static class McpRequest {
        @SerializedName("server_name")
        private String serverName;
        
        @SerializedName("tool_name")
        private String toolName;
        
        private SearchRequest arguments;
    }

    @Data
    private static class SearchRequest {
        private String query;
        private int limit = 10;  // 默认值
        private List<String> engines = Arrays.asList("bing");  // 默认值
    }

    @Data
    private static class SearchResult {
        private String title;
        private String url;
        private String description;
        private String source;
        private String engine;
    }
} 