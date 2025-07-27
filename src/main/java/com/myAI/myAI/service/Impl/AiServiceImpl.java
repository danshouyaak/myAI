package com.myAI.myAI.service.Impl;

import com.alibaba.dashscope.exception.ApiException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myAI.myAI.ai.react.ReactThought;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.config.LangChainConfig;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.mapper.AiMapper;
import com.myAI.myAI.models.entity.Ai;
import com.myAI.myAI.service.AiService;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI服务实现类
 */
@Service
@Slf4j
public class AiServiceImpl extends ServiceImpl<AiMapper, Ai> implements AiService {

    @Resource
    private LangChainConfig.AssistantUnique assistantUnique;

    @Resource
    private ObjectMapper objectMapper;

    private static final long SSE_TIMEOUT = 300000L; // 5 minutes
    private static final long RESPONSE_TIMEOUT_SECONDS = 30;

    @Override
    public String doChat(String message) {
        log.info("开始普通对话，消息：{}", message);
        
        // 使用 CountDownLatch 等待响应完成
        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicReference<StringBuilder> responseBuilder = new AtomicReference<>(new StringBuilder());
        AtomicReference<Throwable> error = new AtomicReference<>();

        try {
            TokenStream stream = assistantUnique.stream("chat", message, "你是一个AI助手");

            stream.onPartialResponse(partialResponse -> {
                responseBuilder.get().append(partialResponse);
            });

            stream.onCompleteResponse(response -> {
                completionLatch.countDown();
            });

            stream.onError(throwable -> {
                error.set(throwable);
                completionLatch.countDown();
            });

            stream.start();

            // 等待响应完成或超时
            if (!completionLatch.await(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new RuntimeException("AI响应超时");
            }

            // 检查是否有错误
            if (error.get() != null) {
                throw new RuntimeException("AI响应错误: " + error.get().getMessage());
            }

            String response = responseBuilder.get().toString().trim();
            if (response.isEmpty()) {
                throw new RuntimeException("AI返回了空响应");
            }

            log.info("对话完成，响应长度：{}", response.length());
            return response;

        } catch (Exception e) {
            log.error("对话失败", e);
            throw new RuntimeException("对话失败: " + e.getMessage());
        }
    }

    @Override
    public SseEmitter doChatStream(String message) {
        log.info("开始流式对话，消息：{}", message);
        
        // 创建SSE发射器
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
        
        try {
            TokenStream stream = assistantUnique.stream("chat", message, "你是一个AI助手");

            // 创建思考对象
            ReactThought thought = new ReactThought();
            thought.setMessageType("ai");
            thought.setMessageTime(formatDate(new Date()));
            thought.setMessageContent("");

            // 用于累积内容的StringBuilder
            StringBuilder contentBuilder = new StringBuilder();

            // 处理部分响应
            stream.onPartialResponse(partialResponse -> {
                try {
                    // 累积内容
                    contentBuilder.append(partialResponse);
                    
                    // 创建新的消息对象
                    ReactThought currentThought = new ReactThought();
                    currentThought.setMessageType("ai");
                    currentThought.setMessageTime(formatDate(new Date()));
                    currentThought.setMessageContent(contentBuilder.toString());
                    
                    // 发送SSE事件
                    String jsonData = objectMapper.writeValueAsString(currentThought);
                    sseEmitter.send(SseEmitter.event()
                        .name("message")
                        .data(jsonData, MediaType.APPLICATION_JSON));
                } catch (IOException e) {
                    log.error("发送消息失败", e);
                    sseEmitter.completeWithError(e);
                }
            });

            // 处理完成响应
            stream.onCompleteResponse(response -> {
                try {
                    // 创建完成消息对象
                    ReactThought completeThought = new ReactThought();
                    completeThought.setMessageType("ai");
                    completeThought.setMessageTime(formatDate(new Date()));
                    completeThought.setMessageContent(contentBuilder.toString());
                    completeThought.setDone(true);
                    
                    // 发送完成消息
                    String jsonData = objectMapper.writeValueAsString(completeThought);
                    sseEmitter.send(SseEmitter.event()
                        .name("message")
                        .data(jsonData, MediaType.APPLICATION_JSON));
                    sseEmitter.complete();
                } catch (IOException e) {
                    log.error("发送完成消息失败", e);
                    sseEmitter.completeWithError(e);
                }
            });

            // 处理错误
            stream.onError(error -> {
                log.error("对话出错", error);
                try {
                    String errorMessage;
                    if (error instanceof ApiException) {
                        ApiException apiError = (ApiException) error;
                        String errorJson = apiError.getMessage();
                        if (errorJson != null && errorJson.contains("Arrearage")) {
                            errorMessage = "AI服务余额不足，请联系管理员充值";
                        } else {
                            errorMessage = "AI服务出错: " + apiError.getMessage();
                        }
                    } else {
                        errorMessage = "对话出错: " + error.getMessage();
                    }
                    
                    // 创建错误消息对象
                    ReactThought errorThought = new ReactThought();
                    errorThought.setMessageType("ai");
                    errorThought.setMessageTime(formatDate(new Date()));
                    errorThought.setMessageContent(errorMessage);
                    errorThought.setDone(true);
                    
                    // 发送错误消息
                    String jsonData = objectMapper.writeValueAsString(errorThought);
                    sseEmitter.send(SseEmitter.event()
                        .name("message")
                        .data(jsonData, MediaType.APPLICATION_JSON));
                } catch (IOException e) {
                    log.error("发送错误消息失败", e);
                }
                sseEmitter.completeWithError(error);
            });

            // 设置超时回调
            sseEmitter.onTimeout(() -> {
                log.warn("SSE连接超时");
                try {
                    // 创建超时消息对象
                    ReactThought timeoutThought = new ReactThought();
                    timeoutThought.setMessageType("ai");
                    timeoutThought.setMessageTime(formatDate(new Date()));
                    timeoutThought.setMessageContent("连接超时，请重试");
                    timeoutThought.setDone(true);
                    
                    // 发送超时消息
                    String jsonData = objectMapper.writeValueAsString(timeoutThought);
                    sseEmitter.send(SseEmitter.event()
                        .name("message")
                        .data(jsonData, MediaType.APPLICATION_JSON));
                } catch (IOException e) {
                    log.error("发送超时消息失败", e);
                }
                sseEmitter.complete();
            });

            // 设置完成回调
            sseEmitter.onCompletion(() -> {
                log.info("SSE连接完成");
            });

            // 开始流式处理
            stream.start();

        } catch (Exception e) {
            log.error("创建流式对话失败", e);
            try {
                // 创建错误消息对象
                ReactThought errorThought = new ReactThought();
                errorThought.setMessageType("ai");
                errorThought.setMessageTime(formatDate(new Date()));
                
                String errorMessage;
                if (e instanceof ApiException) {
                    ApiException apiError = (ApiException) e;
                    String errorJson = apiError.getMessage();
                    if (errorJson != null && errorJson.contains("Arrearage")) {
                        errorMessage = "AI服务余额不足，请联系管理员充值";
                    } else {
                        errorMessage = "AI服务出错: " + apiError.getMessage();
                    }
                } else {
                    errorMessage = "创建对话失败: " + e.getMessage();
                }
                
                errorThought.setMessageContent(errorMessage);
                errorThought.setDone(true);
                
                // 发送错误消息
                String jsonData = objectMapper.writeValueAsString(errorThought);
                sseEmitter.send(SseEmitter.event()
                    .name("message")
                    .data(jsonData, MediaType.APPLICATION_JSON));
            } catch (IOException sendError) {
                log.error("发送错误消息失败", sendError);
            }
            sseEmitter.completeWithError(e);
        }

        return sseEmitter;
    }

    /**
     * 格式化日期
     */
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(date);
    }
}




