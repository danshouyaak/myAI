package com.myAI.myAI.ai.react;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ReAct记忆管理器
 * 负责管理ReAct的对话历史和上下文信息
 */
@Component
@Slf4j
public class ReactMemoryManager {
    
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    
    @Resource
    private ObjectMapper objectMapper;
    
    private static final String REACT_MEMORY_PREFIX = "react:memory:";
    private static final String REACT_SESSION_PREFIX = "react:session:";
    private static final String REACT_CONTEXT_PREFIX = "react:context:";
    
    // 记忆保存时间：7天
    private static final long MEMORY_TTL_DAYS = 7;
    
    // 会话保存时间：1天
    private static final long SESSION_TTL_HOURS = 24;
    
    /**
     * 保存ReAct思考过程
     */
    public void saveThoughtProcess(String sessionId, String userInput, List<ReactThought> thoughts) {
        try {
            String key = REACT_MEMORY_PREFIX + sessionId;
            
            ReactMemory memory = new ReactMemory();
            memory.setSessionId(sessionId);
            memory.setUserInput(userInput);
            memory.setThoughts(thoughts);
            memory.setTimestamp(LocalDateTime.now());
            memory.setThoughtCount(thoughts.size());
            
            String memoryJson = objectMapper.writeValueAsString(memory);
            redisTemplate.opsForValue().set(key, memoryJson, MEMORY_TTL_DAYS, TimeUnit.DAYS);
            
            // 同时保存到会话历史
            saveToSessionHistory(sessionId, memory);
            
            log.info("已保存ReAct记忆，会话ID: {}, 思考步数: {}", sessionId, thoughts.size());
        } catch (Exception e) {
            log.error("保存ReAct记忆失败", e);
        }
    }
    
    /**
     * 获取ReAct思考历史
     */
    public ReactMemory getThoughtHistory(String sessionId) {
        try {
            String key = REACT_MEMORY_PREFIX + sessionId;
            String memoryJson = redisTemplate.opsForValue().get(key);
            
            if (memoryJson != null) {
                ReactMemory memory = objectMapper.readValue(memoryJson, ReactMemory.class);
                log.info("获取ReAct记忆成功，会话ID: {}", sessionId);
                return memory;
            }
        } catch (Exception e) {
            log.error("获取ReAct记忆失败", e);
        }
        
        return null;
    }
    
    /**
     * 保存到会话历史
     */
    private void saveToSessionHistory(String sessionId, ReactMemory memory) {
        try {
            String sessionKey = REACT_SESSION_PREFIX + sessionId;
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 使用Redis List存储会话历史
            String historyItem = String.format("%s|%s|%d", timestamp, memory.getUserInput(), memory.getThoughtCount());
            redisTemplate.opsForList().leftPush(sessionKey, historyItem);
            
            // 限制历史记录数量（最多保存100条）
            redisTemplate.opsForList().trim(sessionKey, 0, 99);
            
            // 设置过期时间
            redisTemplate.expire(sessionKey, SESSION_TTL_HOURS, TimeUnit.HOURS);
            
        } catch (Exception e) {
            log.error("保存会话历史失败", e);
        }
    }
    
    /**
     * 获取会话历史
     */
    public List<String> getSessionHistory(String sessionId) {
        try {
            String sessionKey = REACT_SESSION_PREFIX + sessionId;
            List<String> history = redisTemplate.opsForList().range(sessionKey, 0, -1);
            
            if (history != null && !history.isEmpty()) {
                Collections.reverse(history); // 按时间正序排列
                log.info("获取会话历史成功，会话ID: {}, 记录数: {}", sessionId, history.size());
                return history;
            }
        } catch (Exception e) {
            log.error("获取会话历史失败", e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 保存上下文信息
     */
    public void saveContext(String sessionId, String contextKey, Object contextValue) {
        try {
            String key = REACT_CONTEXT_PREFIX + sessionId + ":" + contextKey;
            String valueJson = objectMapper.writeValueAsString(contextValue);
            
            redisTemplate.opsForValue().set(key, valueJson, SESSION_TTL_HOURS, TimeUnit.HOURS);
            log.debug("保存上下文信息: {} = {}", contextKey, valueJson);
        } catch (Exception e) {
            log.error("保存上下文信息失败", e);
        }
    }
    
    /**
     * 获取上下文信息
     */
    public <T> T getContext(String sessionId, String contextKey, Class<T> valueType) {
        try {
            String key = REACT_CONTEXT_PREFIX + sessionId + ":" + contextKey;
            String valueJson = redisTemplate.opsForValue().get(key);
            
            if (valueJson != null) {
                T value = objectMapper.readValue(valueJson, valueType);
                log.debug("获取上下文信息: {} = {}", contextKey, valueJson);
                return value;
            }
        } catch (Exception e) {
            log.error("获取上下文信息失败", e);
        }
        
        return null;
    }
    
    /**
     * 清理会话数据
     */
    public void clearSession(String sessionId) {
        try {
            // 清理记忆
            String memoryKey = REACT_MEMORY_PREFIX + sessionId;
            redisTemplate.delete(memoryKey);
            
            // 清理会话历史
            String sessionKey = REACT_SESSION_PREFIX + sessionId;
            redisTemplate.delete(sessionKey);
            
            // 清理上下文信息
            String contextPattern = REACT_CONTEXT_PREFIX + sessionId + ":*";
            Set<String> contextKeys = redisTemplate.keys(contextPattern);
            if (contextKeys != null && !contextKeys.isEmpty()) {
                redisTemplate.delete(contextKeys);
            }
            
            log.info("已清理会话数据，会话ID: {}", sessionId);
        } catch (Exception e) {
            log.error("清理会话数据失败", e);
        }
    }
    
    /**
     * 获取相关的历史思考
     */
    public List<ReactThought> getRelevantHistory(String sessionId, String currentInput, int maxResults) {
        try {
            ReactMemory memory = getThoughtHistory(sessionId);
            if (memory == null || memory.getThoughts() == null) {
                return new ArrayList<>();
            }
            
            List<ReactThought> relevantThoughts = new ArrayList<>();
            
            // 简单的相关性匹配：基于关键词
            String[] keywords = currentInput.toLowerCase().split("\\s+");
            
            for (ReactThought thought : memory.getThoughts()) {
                if (isRelevant(thought, keywords)) {
                    relevantThoughts.add(thought);
                    if (relevantThoughts.size() >= maxResults) {
                        break;
                    }
                }
            }
            
            log.info("找到 {} 个相关的历史思考", relevantThoughts.size());
            return relevantThoughts;
            
        } catch (Exception e) {
            log.error("获取相关历史思考失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 判断思考是否与当前输入相关
     */
    private boolean isRelevant(ReactThought thought, String[] keywords) {
        if (thought.getThought() == null && thought.getObservation() == null) {
            return false;
        }
        
        String content = (thought.getThought() + " " + thought.getObservation()).toLowerCase();
        
        for (String keyword : keywords) {
            if (keyword.length() > 2 && content.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics(String sessionId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            ReactMemory memory = getThoughtHistory(sessionId);
            List<String> sessionHistory = getSessionHistory(sessionId);
            
            stats.put("sessionId", sessionId);
            stats.put("hasMemory", memory != null);
            stats.put("thoughtCount", memory != null ? memory.getThoughtCount() : 0);
            stats.put("sessionHistoryCount", sessionHistory.size());
            stats.put("lastActivity", memory != null ? memory.getTimestamp() : null);
            
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
        }
        
        return stats;
    }
    
    /**
     * ReAct记忆数据结构
     */
    public static class ReactMemory {
        private String sessionId;
        private String userInput;
        private List<ReactThought> thoughts;
        private LocalDateTime timestamp;
        private int thoughtCount;
        
        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getUserInput() { return userInput; }
        public void setUserInput(String userInput) { this.userInput = userInput; }
        
        public List<ReactThought> getThoughts() { return thoughts; }
        public void setThoughts(List<ReactThought> thoughts) { this.thoughts = thoughts; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public int getThoughtCount() { return thoughtCount; }
        public void setThoughtCount(int thoughtCount) { this.thoughtCount = thoughtCount; }
    }
}
