package com.myAI.myAI.config;

import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class WebSearchInit {
    final SearchConfig searchConfig;

    @Bean
    public SearchApiWebSearchEngine initWebSearchEngine() {
        System.out.println("searchConfig.getEngine() = " + searchConfig.getEngine());
        System.out.println("searchConfig.getApiKey()= "+ searchConfig.getApiKey());
      return SearchApiWebSearchEngine.builder()
              .engine(searchConfig.getEngine())
              .apiKey(searchConfig.getApiKey())
              .build();
    }
}
