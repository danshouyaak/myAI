package com.myAI.myAI.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {
    // 非记忆版
    @Bean
    public Assistant assistant(ChatLanguageModel qwenChatModel, StreamingChatLanguageModel qwenStreamingChatModel) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        Assistant assistant = AiServices.builder(Assistant.class).chatLanguageModel(qwenChatModel).streamingChatLanguageModel(qwenStreamingChatModel).chatMemory(chatMemory).build();
        return assistant;
    }

    // 记忆版
    @Bean
    public AssistantUnique assistantUnique(ChatLanguageModel qwenChatModel, StreamingChatLanguageModel qwenStreamingChatModel) {
//maxMessages 设置为 10，每次对话只保留 10 条消息
        AssistantUnique assistant = AiServices.builder(AssistantUnique.class).chatLanguageModel(qwenChatModel).streamingChatLanguageModel(qwenStreamingChatModel).chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder().maxMessages(10).id(memoryId).build()).build();

        return assistant;
    }

    public interface Assistant {
        String chat(String message);

        // 流式响应
        TokenStream stream(String message);
    }

    public interface AssistantUnique {

        String chat(@MemoryId Long memoryId, @UserMessage String userMessage);

        // 流式响应
        TokenStream stream(@MemoryId String memoryId, @UserMessage String userMessage);

    }
}
