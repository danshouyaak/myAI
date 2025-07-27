package com.myAI.myAI.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.service.MessageService;
import com.myAI.myAI.mapper.MessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author yu
 * @description 针对表【message】的数据库操作Service实现
 * @createDate 2025-04-22 13:20:40
 */
@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public Message saveUserMessage(String conversationId, String messageContent) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setMessageContent(messageContent);
        message.setMessageType("user");
        message.setSendTime(new Date());

        boolean saved = this.save(message);
        if (saved) {
            log.info("保存用户消息成功，会话ID：{}，消息ID：{}", conversationId, message.getMessageId());
            return message;
        } else {
            log.error("保存用户消息失败，会话ID：{}", conversationId);
            throw new RuntimeException("保存用户消息失败");
        }
    }

    @Override
    public Message saveAiMessage(String conversationId, String messageContent, Long aiId, String aiUrl) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setMessageContent(messageContent);
        message.setMessageType("ai");
        message.setAiId(aiId);
        message.setAiUrl(aiUrl);
        message.setSendTime(new Date());

        boolean saved = this.save(message);
        if (saved) {
            log.info("保存AI消息成功，会话ID：{}，消息ID：{}", conversationId, message.getMessageId());
            return message;
        } else {
            log.error("保存AI消息失败，会话ID：{}", conversationId);
            throw new RuntimeException("保存AI消息失败");
        }
    }

    @Override
    public Message saveAiMessageWithThinking(String conversationId, String messageContent, String thinkingProcess, Long aiId, String aiUrl) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setMessageContent(messageContent);
        message.setThinkingProcess(thinkingProcess);
        message.setMessageType("ai");
        message.setAiId(aiId);
        message.setAiUrl(aiUrl);
        message.setSendTime(new Date());

        boolean saved = this.save(message);
        if (saved) {
            log.info("保存AI消息（含思考过程）成功，会话ID：{}，消息ID：{}", conversationId, message.getMessageId());
            return message;
        } else {
            log.error("保存AI消息（含思考过程）失败，会话ID：{}", conversationId);
            throw new RuntimeException("保存AI消息失败");
        }
    }

    @Override
    @Async("messageAsyncExecutor")
    public void saveUserMessageAsync(String conversationId, String messageContent) {
        try {
            saveUserMessage(conversationId, messageContent);
            log.info("异步保存用户消息成功，会话ID：{}", conversationId);
        } catch (Exception e) {
            log.error("异步保存用户消息失败，会话ID：{}，错误：{}", conversationId, e.getMessage(), e);
        }
    }

    @Override
    @Async("messageAsyncExecutor")
    public void saveAiMessageWithThinkingAsync(String conversationId, String messageContent, String thinkingProcess, Long aiId, String aiUrl) {
        try {
            saveAiMessageWithThinking(conversationId, messageContent, thinkingProcess, aiId, aiUrl);
            log.info("异步保存AI消息（含思考过程）成功，会话ID：{}，思考过程长度：{}", conversationId, thinkingProcess != null ? thinkingProcess.length() : 0);
        } catch (Exception e) {
            log.error("异步保存AI消息（含思考过程）失败，会话ID：{}，错误：{}", conversationId, e.getMessage(), e);
        }
    }

    @Override
    public List<Message> getMessagesByConversationId(String conversationId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversationId", conversationId)
                   .orderByAsc("sendTime");

        List<Message> messages = this.list(queryWrapper);
        log.info("获取会话消息列表，会话ID：{}，消息数量：{}", conversationId, messages.size());
        return messages;
    }
}




