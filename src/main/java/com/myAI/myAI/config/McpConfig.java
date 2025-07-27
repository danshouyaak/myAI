package com.myAI.myAI.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "mcp")
public class McpConfig {
    private Map<String, ServerConfig> mcpServers;

    @Data
    public static class ServerConfig {
        private TransportConfig transport;
    }

    @Data
    public static class TransportConfig {
        private String type;
        private String url;
    }
}