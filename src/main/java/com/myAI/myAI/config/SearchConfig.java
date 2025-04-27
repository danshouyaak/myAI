package com.myAI.myAI.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "search")
@Configuration
public class SearchConfig {
    private String engine;
    private String apiKey;
}
