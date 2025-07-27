package com.myAI.myAI.ai.react;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ReAct处理器测试
 */
@SpringBootTest
@Slf4j
public class ReactProcessorTest {
    
    @Resource
    private ReactProcessor reactProcessor;
    
    @Resource
    private ReactToolManager toolManager;
    
    @Resource
    private ReactMemoryManager memoryManager;
    
    @Test
    public void testBasicReActFlow() throws InterruptedException {
        log.info("=== 测试基本ReAct流程 ===");
        
        String userInput = "计算 2 + 3 * 4 的结果";
        CountDownLatch latch = new CountDownLatch(1);
        
        SseEmitter emitter = reactProcessor.processStreamSSE(userInput, "test_session_1");
        
        emitter.onCompletion(() -> {
            log.info("ReAct处理完成");
            latch.countDown();
        });
        
        emitter.onError(throwable -> {
            log.error("ReAct处理出错", throwable);
            latch.countDown();
        });
        
        // 等待处理完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("测试超时");
        }
    }
    
    @Test
    public void testSearchFlow() throws InterruptedException {
        log.info("=== 测试搜索流程 ===");
        
        String userInput = "搜索最新的AI技术发展";
        CountDownLatch latch = new CountDownLatch(1);
        
        SseEmitter emitter = reactProcessor.processStreamSSE(userInput, "test_session_2");
        
        emitter.onCompletion(() -> {
            log.info("搜索流程完成");
            latch.countDown();
        });
        
        emitter.onError(throwable -> {
            log.error("搜索流程出错", throwable);
            latch.countDown();
        });
        
        // 等待处理完成
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("搜索测试超时");
        }
    }
    
    @Test
    public void testTextProcessingFlow() throws InterruptedException {
        log.info("=== 测试文本处理流程 ===");
        
        String userInput = "统计这段文本的字数：人工智能是计算机科学的一个分支，它企图了解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。";
        CountDownLatch latch = new CountDownLatch(1);
        
        SseEmitter emitter = reactProcessor.processStreamSSE(userInput, "test_session_3");
        
        emitter.onCompletion(() -> {
            log.info("文本处理完成");
            latch.countDown();
        });
        
        emitter.onError(throwable -> {
            log.error("文本处理出错", throwable);
            latch.countDown();
        });
        
        // 等待处理完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("文本处理测试超时");
        }
    }
    
    @Test
    public void testDateTimeFlow() throws InterruptedException {
        log.info("=== 测试时间日期流程 ===");
        
        String userInput = "计算从2024年1月1日到今天过了多少天";
        CountDownLatch latch = new CountDownLatch(1);
        
        SseEmitter emitter = reactProcessor.processStreamSSE(userInput, "test_session_4");
        
        emitter.onCompletion(() -> {
            log.info("时间日期处理完成");
            latch.countDown();
        });
        
        emitter.onError(throwable -> {
            log.error("时间日期处理出错", throwable);
            latch.countDown();
        });
        
        // 等待处理完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("时间日期测试超时");
        }
    }
    
    @Test
    public void testToolManager() {
        log.info("=== 测试工具管理器 ===");
        
        // 测试工具注册
        int toolCount = toolManager.getToolCount();
        log.info("已注册工具数量: {}", toolCount);
        
        // 测试工具列表
        var toolNames = toolManager.getAllToolNames();
        log.info("可用工具: {}", toolNames);
        
        // 测试工具描述
        String description = toolManager.getAllToolsDescription();
        log.info("工具描述: {}", description);
        
        // 测试工具可用性
        for (String toolName : toolNames) {
            boolean available = toolManager.isToolAvailable(toolName);
            log.info("工具 {} 可用性: {}", toolName, available);
        }
    }
    
    @Test
    public void testMemoryManager() {
        log.info("=== 测试记忆管理器 ===");
        
        String sessionId = "test_memory_session";
        
        // 测试保存记忆
        ReactThought thought1 = new ReactThought();
        thought1.setThought("这是一个测试思考");
        thought1.setAction("CALCULATOR");
        thought1.setActionInput("2+3");
        thought1.setObservation("计算结果: 5");
        
        ReactThought thought2 = new ReactThought();
        thought2.setThought("继续测试");
        thought2.setAction("TEXT_PROCESSOR");
        thought2.setActionInput("count|测试文本");
        thought2.setObservation("字符数: 4");
        
        java.util.List<ReactThought> thoughts = java.util.Arrays.asList(thought1, thought2);
        memoryManager.saveThoughtProcess(sessionId, "测试输入", thoughts);
        
        // 测试获取记忆
        ReactMemoryManager.ReactMemory memory = memoryManager.getThoughtHistory(sessionId);
        if (memory != null) {
            log.info("获取记忆成功: 用户输入={}, 思考数量={}", memory.getUserInput(), memory.getThoughtCount());
        } else {
            log.warn("未找到记忆");
        }
        
        // 测试会话历史
        var history = memoryManager.getSessionHistory(sessionId);
        log.info("会话历史: {}", history);
        
        // 测试统计信息
        var stats = memoryManager.getStatistics(sessionId);
        log.info("统计信息: {}", stats);
        
        // 测试相关记忆
        var relevantMemory = memoryManager.getRelevantHistory(sessionId, "计算", 2);
        log.info("相关记忆数量: {}", relevantMemory.size());
        
        // 清理测试数据
        memoryManager.clearSession(sessionId);
        log.info("已清理测试会话数据");
    }
    
    @Test
    public void testComplexReActFlow() throws InterruptedException {
        log.info("=== 测试复杂ReAct流程 ===");
        
        String userInput = "帮我搜索人工智能的最新发展，然后统计搜索结果的字数，最后计算一下如果每天阅读100字，需要多少天读完";
        CountDownLatch latch = new CountDownLatch(1);
        
        SseEmitter emitter = reactProcessor.processStreamSSE(userInput, "test_complex_session");
        
        emitter.onCompletion(() -> {
            log.info("复杂ReAct流程完成");
            latch.countDown();
        });
        
        emitter.onError(throwable -> {
            log.error("复杂ReAct流程出错", throwable);
            latch.countDown();
        });
        
        // 等待处理完成（复杂流程需要更长时间）
        boolean completed = latch.await(120, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("复杂流程测试超时");
        }
    }
}
