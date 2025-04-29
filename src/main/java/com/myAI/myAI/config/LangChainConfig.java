package com.myAI.myAI.config;

import com.myAI.myAI.langchain.PersistentChatMemoryStore;
import com.myAI.myAI.langchain.service.ToolsService;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.*;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.web.search.WebSearchTool;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class LangChainConfig {
    // 非记忆版
    @Bean
    public Assistant assistant(ChatLanguageModel qwenChatModel, StreamingChatLanguageModel qwenStreamingChatModel) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .build();
        return assistant;
    }

    @Resource
    private RedisTemplate<String, String> redisTemplate;

//    创建一个向量数据库
    @Bean
    public EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore();
    }


//    质谱ai
    @Bean
    public ZhipuAiStreamingChatModel zhipuAiStreamingChatModel() {
        return  ZhipuAiStreamingChatModel.builder()
                .apiKey("7d87de8424d64f239da60fcd2fbf8ea8.mhKGQxgcVD21XoSW")
                .logRequests(true)
                .logResponses(true)
                .callTimeout(Duration.ofSeconds(60))
                .connectTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .build();
    }


    // 记忆版
    @Bean
    public AssistantUnique assistantUniqueStore(ChatLanguageModel qwenChatModel,
                                                StreamingChatLanguageModel zhipuAiStreamingChatModel,
                                                SearchApiWebSearchEngine searchApiWebSearchEngine,
                                                EmbeddingStore embeddingStore,
                                                QwenEmbeddingModel qwenEmbeddingModel) {

        PersistentChatMemoryStore store = new PersistentChatMemoryStore(redisTemplate);

//        内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)  // 绑定向量数据库
                .embeddingModel(qwenEmbeddingModel)   // 绑定向量模型
                .maxResults(5) // 最相似的5个结果
                .minScore(0.6) // 只找相似度在0.6以上的内容
                .build();


        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();


        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
//                .toolProvider(toolProvider)
                .tools(new ToolsService(),new WebSearchTool(searchApiWebSearchEngine))
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(zhipuAiStreamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(contentRetriever)  // 绑定内容检索器
                .build();
        return assistant;
    }

    public interface Assistant {
        String chat(String message);

        // 流式响应
        TokenStream stream(String message);
    }

    public interface AssistantUnique {

        String chat(@MemoryId Long memoryId, @UserMessage String userMessage);

        @SystemMessage("这是你的的角色 请严格遵守{{description}}")
            // 流式响应
        TokenStream stream(@MemoryId String memoryId, @UserMessage String userMessage, @V("description") String description);

        TokenStream stream1(@MemoryId String memoryId, @UserMessage String userMessage);
    }
}
