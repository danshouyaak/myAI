package com.myAI.myAI.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myAI.myAI.models.entity.Ai;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI服务接口
 */
public interface AiService extends IService<Ai> {

    /**
     * AI对话（普通模式）
     *
     * @param message 用户输入的消息
     * @return AI的回复
     */
    String doChat(String message);

    /**
     * AI对话（流式输出）
     *
     * @param message 用户输入的消息
     * @return SSE发射器
     */
    SseEmitter doChatStream(String message);
}
