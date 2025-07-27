package com.myAI.myAI.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myAI.myAI.models.entity.Message;

import java.util.List;

/**
 * @author yu
 * @description 针对表【message】的数据库操作Service
 * @createDate 2025-04-22 13:20:40
 */
public interface MessageService extends IService<Message> {

    /**
     * 保存用户消息
     * @param conversationId 会话ID
     * @param messageContent 消息内容
     * @return 保存的消息
     */
    Message saveUserMessage(String conversationId, String messageContent);

    /**
     * 保存AI消息
     * @param conversationId 会话ID
     * @param messageContent 消息内容
     * @param aiId AI ID
     * @param aiUrl AI 头像URL
     * @return 保存的消息
     */
    Message saveAiMessage(String conversationId, String messageContent, Long aiId, String aiUrl);

    /**
     * 保存AI消息（包含思考过程）
     * @param conversationId 会话ID
     * @param messageContent 消息内容
     * @param thinkingProcess 思考过程（JSON格式）
     * @param aiId AI ID
     * @param aiUrl AI 头像URL
     * @return 保存的消息
     */
    Message saveAiMessageWithThinking(String conversationId, String messageContent, String thinkingProcess, Long aiId, String aiUrl);

    /**
     * 异步保存用户消息
     * @param conversationId 会话ID
     * @param messageContent 消息内容
     */
    void saveUserMessageAsync(String conversationId, String messageContent);

    /**
     * 异步保存AI消息（包含思考过程）
     * @param conversationId 会话ID
     * @param messageContent 消息内容
     * @param thinkingProcess 思考过程（JSON格式）
     * @param aiId AI ID
     * @param aiUrl AI 头像URL
     */
    void saveAiMessageWithThinkingAsync(String conversationId, String messageContent, String thinkingProcess, Long aiId, String aiUrl);

    /**
     * 根据会话ID获取消息列表
     * @param conversationId 会话ID
     * @return 消息列表
     */
    List<Message> getMessagesByConversationId(String conversationId);
}
