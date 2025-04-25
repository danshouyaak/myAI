package com.myAI.myAI.config;

import com.myAI.myAI.langchain.PersistentChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.*;
import dev.langchain4j.store.memory.chat.redis.RedisChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.collections.RedisStore;

import javax.annotation.Resource;

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


    // 记忆版
    @Bean
    public AssistantUnique assistantUniqueStore(ChatLanguageModel qwenChatModel,
                                                StreamingChatLanguageModel qwenStreamingChatModel) {

        PersistentChatMemoryStore store = new PersistentChatMemoryStore(redisTemplate);

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();

        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder().maxMessages(10)
                                .id(memoryId).build()
                )
                .chatMemoryProvider(chatMemoryProvider)
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

    }
}
