package com.myAI.myAI.langchain;

import com.myAI.myAI.config.RedisConfig;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

/**
 * 自定义的持久化聊天存储器
 */

@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {


    private final RedisTemplate<String, String> redisTemplate;


    public PersistentChatMemoryStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String s = redisTemplate.opsForValue().get(memoryId);
        return messagesFromJson(s);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = messagesToJson(messages); // 序列化消息
        redisTemplate.opsForValue().set((String) memoryId, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete((String) memoryId);
    }

}