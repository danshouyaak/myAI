package com.myAI.myAI.task;


import com.zhipu.oapi.service.v4.image.Image;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiImageModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;

import java.net.URI;
import java.time.Duration;


public class SearchApiTool {
    public static void main(String[] args) {
        ChatLanguageModel zhipuAiChatModel = ZhipuAiChatModel.builder()
                .apiKey("7d87de8424d64f239da60fcd2fbf8ea8.mhKGQxgcVD21XoSW")
                .callTimeout(Duration.ofSeconds(60))
                .connectTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .build();

        String chat = zhipuAiChatModel.chat("你好");
        System.out.println(chat);
    }
}