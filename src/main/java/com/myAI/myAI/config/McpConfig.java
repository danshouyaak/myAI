package com.myAI.myAI.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.Map;

@Configuration
public class McpConfig {
    @Value("${BAIDU_MAP_API_KEY}") // 从配置注入环境变量
    private String baiduMapApiKey;
    @Bean
    public McpTransport mcpTransport() {
        // 检查环境变量
        String apiKey = baiduMapApiKey;
        if (apiKey == null) {
            throw new IllegalStateException("BAIDU_MAP_API_KEY 环境变量未配置！");
        }

        return new StdioMcpTransport.Builder()
                .command(List.of("cmd", "/c", "npx", "-y", "@baidumap/mcp-server-baidu-map", "mcp/github"))
                .environment(Map.of("BAIDU_MAP_API_KEY", apiKey)) // 修正 Map.of 键名拼写错误
                .logEvents(true)
                .build();
    }

    @Bean
    public McpClient mcpClient(McpTransport transport) {
        return new DefaultMcpClient.Builder()
                .transport(transport)
                .build();
    }

    @Bean
    public McpToolProvider mcpToolProvider(McpClient mcpClient) {
        return McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();
    }
}