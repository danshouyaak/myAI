package com.myAI.myAI.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.Map;


public class test {
    public static void main(String[] args) throws Exception {
        // 1.构建模型
        ChatLanguageModel model = QwenChatModel
                .builder()
                .apiKey("sk-83365e2d612a4576b14ba1f823af2b10")
                .modelName("qwen-max")
                .build();

        // 2.构建MCP服务传输方式  有sse和stdio两种， 这里演示的是stdio
        McpTransport transport = new StdioMcpTransport.Builder()
                .command(List.of("cmd",
                        "/c",
                        "npx",
                        "-y", "serper-search-scrape-mcp-server"))
                .environment(Map.of("SERPER_API_KEY",
                        "37109a07-e86e-4a7a-b95c-153af5509ab7"))
                .logEvents(true)
                .build();

        // 3.构建MCP客户端， 指定传输方式
        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        // 4.构建MCP工具提供者， 指定MCP客户端
        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();

        // 5.构建服务代理， 指定模型和工具提供者
        Bot bot = AiServices.builder(Bot.class)
                .chatLanguageModel(model)
                .toolProvider(toolProvider)
                .build();

        try {
            // 对话请求
            String response = bot.chat("规划长沙到武汉骑行路线");
            System.out.println("RESPONSE: " + response);
        } finally {
            mcpClient.close();
        }

    }
    interface Bot {

        String chat(String userMessage);
    }
}
