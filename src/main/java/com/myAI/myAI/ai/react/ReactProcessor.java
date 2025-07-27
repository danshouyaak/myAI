package com.myAI.myAI.ai.react;

import com.google.gson.Gson;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.config.LangChainConfig;
import com.myAI.myAI.exception.BusinessException;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * ReAct核心处理器
 * 实现ReAct的核心逻辑：思考(Reasoning) -> 行动(Acting) -> 观察(Observing)的循环
 */
@Component
@Data
@Slf4j
public class ReactProcessor {

    @Resource
    private ReactToolManager toolManager;

    @Resource
    @Qualifier("reactQwenStreamingChatModel")
    private StreamingChatLanguageModel streamingChatLanguageModel;

    @Resource
    private Gson gson;

    @Resource
    private ObjectMapper objectMapper;  // Spring Boot 自动配置的 ObjectMapper

    @Resource
    private ReactErrorHandler errorHandler;

    @Resource
    private ReactMemoryManager memoryManager;

    @Resource
    private com.myAI.myAI.service.MessageService messageService;

    @Resource
    private com.myAI.myAI.service.ConversationService conversationService;

    @Resource
    private com.myAI.myAI.service.AiModelService aiModelService;

    /**
     * 获取AI模型的专用prompt
     */
    private String getAiModelPrompt(String conversationId) {
        try {
            // 获取会话信息
            var conversationVO = conversationService.getConversationWithAiInfo(conversationId);
            if (conversationVO != null && conversationVO.getAiId() != null) {
                // 获取AI模型详情
                var aiModel = aiModelService.getById(Long.valueOf(conversationVO.getAiId()));
                if (aiModel != null && aiModel.getPrompt() != null && !aiModel.getPrompt().trim().isEmpty()) {
                    log.info("🤖 使用AI模型专用prompt: {}", aiModel.getName());
                    return aiModel.getPrompt();
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ 获取AI模型prompt失败，使用默认prompt: {}", e.getMessage());
        }

        log.info("🔄 使用默认ReAct prompt");
        return REACT_SYSTEM_PROMPT;
    }

    /**
     * ReAct系统角色描述（默认）
     */
    private static final String REACT_SYSTEM_PROMPT =
        "你是一个遵循 ReAct (Reasoning + Acting) 模式的智能助手。你需要通过系统性的思考、行动和观察来解决问题。\n\n" +

        "## 工作流程：\n" +
        "1. **思考(Thought)**: 分析问题，制定解决策略\n" +
        "2. **行动(Action)**: 选择并执行合适的工具\n" +
        "3. **观察(Observation)**: 分析工具执行结果\n" +
        "4. **评估**: 判断是否需要继续或已达成目标\n\n" +

        "## 思考阶段要求：\n" +
        "- 明确理解用户的问题和需求\n" +
        "- 分析当前已有的信息\n" +
        "- 确定下一步需要获取什么信息或执行什么操作\n" +
        "- 选择最合适的工具来完成任务\n\n" +

        "## 行动阶段要求：\n" +
        "- 严格按照格式：工具名称|具体参数\n" +
        "- 确保参数准确且完整\n" +
        "- 一次只执行一个工具\n" +
        "- 如果工具不可用，选择替代方案\n\n" +

        "## 观察阶段要求：\n" +
        "- 仔细分析工具返回的结果\n" +
        "- 判断结果是否满足当前需求\n" +
        "- 确定是否需要进一步的行动\n" +
        "- 如果信息充足，准备给出最终答案\n\n" +

        "## 重要规则：\n" +
        "- 保持逻辑清晰，步骤明确\n" +
        "- 每个行动都要有明确目的\n" +
        "- 避免重复无效的操作\n" +
        "- 当获得足够信息时，使用 TERMINATE 工具给出最终答案\n" +
        "- 如果遇到错误，分析原因并尝试其他方法\n" +
        "- 完成任务后必须使用 TERMINATE 工具结束对话\n\n" +

        "## 特殊工具说明：\n" +
        "- TERMINATE: 当你已经得到问题的答案或完成所有必要任务时，使用此工具结束对话\n" +
        "- 使用格式: Action: TERMINATE\\nAction Input: [你的最终答案]";

    /**
     * 最大思考次数，防止无限循环
     */
    private static final int MAX_ITERATIONS = 5;

    /**
     * 工具执行最大重试次数
     */
    private static final int MAX_TOOL_RETRIES = 3;

    private static final int MAX_RETRIES = 3;
    private static final long RESPONSE_TIMEOUT_SECONDS = 30;
    private static final long SSE_TIMEOUT = 300000L; // 5 minutes

    // 代理状态
    private ReactState state = ReactState.IDLE;
    private int currentStep = 0;
    private List<ReactThought> thoughts = new ArrayList<>();

    // 当前会话的AI模型prompt
    private String currentAiModelPrompt = REACT_SYSTEM_PROMPT;

    /**
     * 流式处理用户输入（返回SSE）
     */
    public SseEmitter processStreamSSE(String userInput) {
        return processStreamSSE(userInput, null);
    }

    /**
     * 流式处理用户输入（返回SSE）- 带会话ID
     */
    public SseEmitter processStreamSSE(String userInput, String sessionId) {
        return processStreamSSE(userInput, sessionId, null);
    }

    /**
     * 流式处理用户输入（返回SSE）- 带会话ID和用户ID
     */
    public SseEmitter processStreamSSE(String userInput, String conversationId, Long userId) {
        // 创建SSE发射器
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);

        // 使用传入的 conversationId 作为会话ID
        final String finalSessionId = conversationId;

        // 验证会话ID和用户ID
        log.info("🔍 验证请求参数 - 会话ID：{}，用户ID：{}，输入内容：{}", conversationId, userId, userInput);

        // 保存用户消息到数据库
        if (userId != null) {
            log.info("🔄 开始保存用户消息，会话ID：{}，用户ID：{}，消息内容：{}", conversationId, userId, userInput);
            try {
                // 先尝试同步保存，确保能正常保存
                messageService.saveUserMessage(conversationId, userInput);
                log.info("✅ 用户消息同步保存成功，会话ID：{}，用户ID：{}", conversationId, userId);

                // 然后异步保存（如果需要的话）
                // messageService.saveUserMessageAsync(conversationId, userInput);
            } catch (Exception e) {
                log.error("❌ 用户消息保存失败，会话ID：{}，用户ID：{}，错误：{}", conversationId, userId, e.getMessage(), e);
            }
        } else {
            log.warn("⚠️ 用户ID为空，无法保存用户消息，会话ID：{}", conversationId);
        }

        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 基础校验
                if (this.state != ReactState.IDLE) {
                    sendErrorAndComplete(sseEmitter, "当前状态无法处理新请求：" + this.state);
                    return;
                }
                if (userInput == null || userInput.trim().isEmpty()) {
                    sendErrorAndComplete(sseEmitter, "输入不能为空");
                    return;
                }

                // 2. 开始处理
                this.state = ReactState.RUNNING;
                log.info("开始流式处理用户输入: {}, 会话ID: {}", userInput, finalSessionId);

                // 获取AI模型的专用prompt
                currentAiModelPrompt = getAiModelPrompt(conversationId);
                log.info("🤖 当前会话使用的prompt: {}", currentAiModelPrompt.substring(0, Math.min(100, currentAiModelPrompt.length())) + "...");

                // 发送开始事件
                sendEvent(sseEmitter, "start", "开始处理请求...");
                sendEvent(sseEmitter, "session", finalSessionId);

                // 3. 加载相关历史记忆
                loadRelevantMemory(userInput, finalSessionId, sseEmitter);

                // 4. 执行ReAct循环
                executeReActLoop(userInput, sseEmitter);

                // 5. 保存思考过程到记忆和数据库
                saveThoughtProcess(userInput, finalSessionId, userId);



                // 7. 发送完成事件
                sendEvent(sseEmitter, "complete", "处理完成");

                // 8. 设置完成状态
                this.state = ReactState.FINISHED;

                // 6. 正常完成
                sseEmitter.complete();

            } catch (Exception e) {
                handleError(sseEmitter, e);
            }
        });

        // 设置超时和完成回调
        setupSseCallbacks(sseEmitter);
        
        return sseEmitter;
    }

    /**
     * 执行ReAct循环
     */
    private void executeReActLoop(String userInput, SseEmitter sseEmitter) throws IOException {
        currentStep = 0;
        thoughts.clear();

        while (currentStep < MAX_ITERATIONS && state == ReactState.RUNNING) {
            currentStep++;
            log.info("========== 开始第 {} 轮思考 (最大 {} 轮) ==========", currentStep, MAX_ITERATIONS);

            // 发送进度事件
            sendEvent(sseEmitter, "progress", String.format("第 %d/%d 轮思考", currentStep, MAX_ITERATIONS));

            ReactThought thought = new ReactThought();
            thoughts.add(thought);

            try {
                // 1. 思考阶段
                log.info(">>> 第 {} 轮 - 步骤1: 思考阶段", currentStep);
                executeThinkingPhase(thought, userInput, sseEmitter);

                // 检查思考阶段是否已经完成（包含终止指令）
                if (thought.isDone() && thought.getFinalAnswer() != null) {
                    log.info("第 {} 轮 - 思考阶段已完成任务，跳过后续步骤", currentStep);
                    break;
                }

                // 2. 行动阶段
                log.info(">>> 第 {} 轮 - 步骤2: 行动阶段", currentStep);
                executeActionPhase(thought, sseEmitter);

                // 3. 观察阶段
                log.info(">>> 第 {} 轮 - 步骤3: 观察阶段", currentStep);
                executeObservationPhase(thought, sseEmitter);

                // 检查是否调用了终止工具
                if ("TERMINATE".equalsIgnoreCase(thought.getAction()) && thought.isDone()) {
                    log.info("第 {} 轮 - 检测到终止工具调用，结束思考循环", currentStep);
                    break;
                }

                // 4. 评估是否继续
                log.info(">>> 第 {} 轮 - 步骤4: 评估是否继续", currentStep);
                if (evaluateContinuation(thought, sseEmitter)) {
                    log.info("第 {} 轮 - 决定停止，生成最终答案", currentStep);
                    break;
                } else {
                    log.info("第 {} 轮 - 决定继续下一轮思考", currentStep);
                }

            } catch (Exception e) {
                log.error("第 {} 轮处理发生错误", currentStep, e);
                thought.setObservation("处理过程发生错误: " + e.getMessage());
                thought.setMessageType("error");
                sendThought(sseEmitter, thought);
                // 不中断整个过程，继续下一轮
                log.info("第 {} 轮出错，继续下一轮", currentStep);
            }

            log.info("========== 第 {} 轮思考完成 ==========", currentStep);
        }

        // 检查是否达到最大步数
        if (currentStep >= MAX_ITERATIONS) {
            handleMaxStepsReached(sseEmitter);
        }
    }

    /**
     * 发送SSE事件 - 使用 JSON 格式
     */
    private void sendEvent(SseEmitter sseEmitter, String eventName, String data) throws IOException {
        try {
            // 检查 SseEmitter 是否仍然可用
            if (sseEmitter == null) {
                log.warn("SseEmitter 为 null，跳过发送事件: {}", eventName);
                return;
            }

            // 检查当前状态
            if (this.state == ReactState.ERROR || this.state == ReactState.FINISHED) {
                log.warn("ReactProcessor 已完成或出错，跳过发送事件: {}", eventName);
                return;
            }

            // 创建统一的事件对象
            ReactThought eventThought = new ReactThought();
            eventThought.setMessageType(eventName);
            eventThought.setMessageContent(data);

            // 根据事件类型设置不同的字段
            switch (eventName) {
                case "start":
                case "progress":
                case "memory":
                    eventThought.setThought(data);
                    break;
                case "error":
                    eventThought.setObservation("❌ " + data);
                    break;
                case "complete":
                    eventThought.setDone(true);
                    eventThought.setThought("✅ " + data);
                    break;
                default:
                    eventThought.setThought(data);
                    break;
            }

            // 使用统一的 JSON 发送方法
            sendSSEData(sseEmitter, "message", eventThought);
        } catch (IllegalStateException e) {
            log.warn("SseEmitter 已完成，无法发送事件 {}: {}", eventName, e.getMessage());
        }
    }

    /**
     * 执行思考阶段
     */
    private void executeThinkingPhase(ReactThought thought, String userInput, SseEmitter sseEmitter) throws IOException {
        log.info("第 {} 轮 - 思考阶段开始", currentStep);
        String thoughtPrompt = generateThoughtPrompt(userInput, thoughts);
        log.debug("思考提示词: {}", thoughtPrompt);

        thought.setThought("🤔 正在分析问题...");
        thought.setMessageType("thinking");
        sendThought(sseEmitter, thought);
        log.info("已发送思考开始状态");

        try {
            // 使用简化的流式处理
            log.info("开始调用AI进行思考...");
            String thoughtResult = streamAIResponse(thoughtPrompt, REACT_SYSTEM_PROMPT);
            log.info("AI思考原始结果: {}", thoughtResult);

            String cleanedThought = cleanThoughtContent(thoughtResult);
            log.info("清理后的思考内容: {}", cleanedThought);

            // 检查思考内容是否包含终止指令
            if (cleanedThought.contains("Action: TERMINATE")) {
                log.info("在思考阶段检测到终止指令，直接处理");

                // 解析终止指令
                String[] actionDetails = parseActionPlan(cleanedThought);
                if ("TERMINATE".equalsIgnoreCase(actionDetails[0])) {
                    thought.setThought(cleanedThought);
                    thought.setAction(actionDetails[0]);
                    thought.setActionInput(actionDetails[1]);
                    thought.setMessageType("thinking");
                    thought.setMessageContent(cleanedThought);
                    sendThought(sseEmitter, thought);

                    // 直接执行终止逻辑
                    log.info("直接执行终止工具，最终答案: {}", actionDetails[1]);
                    thought.setObservation("✅ 任务完成，生成最终答案");
                    thought.setFinalAnswer(actionDetails[1]);
                    thought.setDone(true);
                    thought.setMessageType("ai");
                    thought.setMessageContent(actionDetails[1]);
                    sendThought(sseEmitter, thought);

                    log.info("思考阶段终止工具执行完成，最终答案: {}", actionDetails[1]);
                    return; // 直接返回，跳过后续的行动和观察阶段
                }
            }

            thought.setThought(cleanedThought);
            thought.setMessageType("thinking");
            thought.setMessageContent(cleanedThought);
            sendThought(sseEmitter, thought);
            log.info("已发送思考完成状态");

            log.info("第 {} 轮 - 思考完成: {}", currentStep, thought.getThought());
        } catch (Exception e) {
            log.error("思考阶段发生错误", e);
            throw e;
        }
    }

    /**
     * 清理思考内容，移除不必要的格式
     */
    private String cleanThoughtContent(String content) {
        if (content == null) return "";

        // 移除多余的换行和空格
        content = content.trim().replaceAll("\\n+", "\n");

        // 确保思考内容有适当的结构
        if (!content.isEmpty() && !content.startsWith("思考:") && !content.startsWith("Thought:")) {
            content = "💭 " + content;
        }

        return content;
    }

    /**
     * 执行行动阶段
     */
    private void executeActionPhase(ReactThought thought, SseEmitter sseEmitter) throws IOException {
        log.info("第 {} 轮 - 行动阶段开始", currentStep);
        String actionPrompt = generateActionPrompt(thought.getThought(), toolManager.getAllToolsDescription());
        log.debug("行动提示词: {}", actionPrompt);
        log.info("可用工具: {}", toolManager.getAllToolsDescription());

        thought.setAction("正在选择行动...");
        sendThought(sseEmitter, thought);
        log.info("已发送行动开始状态");

        try {
            // 使用简化的流式处理
            log.info("开始调用AI选择行动...");
            String actionPlan = streamAIResponse(actionPrompt, "你是一个AI助手");
            log.info("AI行动计划原始结果: {}", actionPlan);

            String[] actionDetails = parseActionPlan(actionPlan);
            log.info("解析的行动详情: 工具={}, 参数={}", actionDetails[0], actionDetails[1]);

            thought.setAction(actionDetails[0]);
            thought.setActionInput(actionDetails[1]);
            thought.setMessageType("ai");  // 添加消息类型
            thought.setMessageContent(String.format("行动：%s\n参数：%s", actionDetails[0], actionDetails[1]));  // 添加消息内容
            sendThought(sseEmitter, thought);
            log.info("已发送行动计划完成状态");
        } catch (Exception e) {
            log.error("行动阶段发生错误", e);
            throw e;
        }
    }

    /**
     * 执行观察阶段
     */
    private void executeObservationPhase(ReactThought thought, SseEmitter sseEmitter) throws IOException {
        log.info("第 {} 轮 - 观察阶段开始，执行行动: {} 参数: {}", currentStep, thought.getAction(), thought.getActionInput());

        // 验证行动参数
        if (thought.getAction() == null || thought.getAction().trim().isEmpty()) {
            log.warn("行动为空，跳过执行");
            thought.setObservation("❌ 未指定有效的行动，请重新思考");
            thought.setMessageType("error");
            sendThought(sseEmitter, thought);
            return;
        }

        thought.setObservation(String.format("🔧 正在执行: %s", thought.getAction()));
        thought.setMessageType("action");
        thought.setMessageContent(String.format("正在执行工具: %s", thought.getAction()));
        sendThought(sseEmitter, thought);
        log.info("已发送工具执行开始状态");

        try {
            // 执行行动，带重试机制
            log.info("开始执行工具: {} 参数: {}", thought.getAction(), thought.getActionInput());
            String observation = executeActionWithRetry(thought.getAction(), thought.getActionInput());
            log.info("工具执行完成，结果: {}", observation);

            // 检查是否是终止工具
            if ("TERMINATE".equalsIgnoreCase(thought.getAction())) {
                log.info("检测到终止工具调用，准备生成最终答案");

                // 从观察结果中提取最终答案
                String finalAnswer = thought.getActionInput(); // 使用工具的输入作为最终答案
                if (observation.startsWith("FINAL_ANSWER:")) {
                    finalAnswer = observation.substring("FINAL_ANSWER:".length()).trim();
                }

                thought.setObservation("✅ 任务完成，生成最终答案");
                thought.setMessageType("observation");
                thought.setMessageContent("✅ 任务完成，生成最终答案");
                sendThought(sseEmitter, thought);

                // 设置最终答案
                thought.setFinalAnswer(finalAnswer);
                thought.setDone(true);
                thought.setMessageType("ai");
                thought.setMessageContent(finalAnswer);
                sendThought(sseEmitter, thought);

                log.info("终止工具执行完成，最终答案: {}", finalAnswer);
                return; // 直接返回，不继续后续流程
            }

            thought.setObservation(observation);
            thought.setMessageType("observation");
            thought.setMessageContent(observation);

            sendThought(sseEmitter, thought);
            log.info("已发送观察结果完成状态");
            log.info("第 {} 轮 - 观察阶段完成", currentStep);
        } catch (Exception e) {
            log.error("观察阶段发生错误", e);
            thought.setObservation("❌ 工具执行失败: " + e.getMessage());
            thought.setMessageType("error");
            sendThought(sseEmitter, thought);
            throw e;
        }
    }

    /**
     * 带重试机制的行动执行
     */
    private String executeActionWithRetry(String action, String input) {
        Exception lastException = null;

        for (int retry = 0; retry < MAX_TOOL_RETRIES; retry++) {
            try {
                String result = executeAction(action, input);
                if (retry > 0) {
                    log.info("工具执行在第 {} 次重试后成功", retry + 1);
                }
                return "✅ " + result;
            } catch (Exception e) {
                lastException = e;
                log.warn("工具执行失败，第 {} 次重试: {}", retry + 1, e.getMessage());

                // 使用错误处理器判断是否应该重试
                if (!errorHandler.shouldRetry(e, retry, MAX_TOOL_RETRIES)) {
                    log.info("错误处理器建议不再重试");
                    break;
                }

                // 计算重试延迟
                long delay = errorHandler.calculateRetryDelay(retry);
                try {
                    log.info("等待 {} 毫秒后重试", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // 所有重试都失败了，使用错误处理器生成恢复建议
        String recoveryPrompt = errorHandler.generateRecoveryPrompt(lastException, action, input);
        log.error("工具执行最终失败，生成恢复建议: {}", recoveryPrompt);
        return "❌ " + recoveryPrompt;
    }

    /**
     * 格式化错误消息
     */
    private String formatErrorMessage(Exception e) {
        if (e == null) return "未知错误";

        String message = e.getMessage();
        if (message.contains("Connection refused") && message.contains("localhost:3000")) {
            return "MCP服务未启动或无法连接，请确保MCP服务正在运行";
        } else if (message.contains("timeout")) {
            return "工具执行超时，请检查网络连接或服务状态";
        } else if (message.contains("Tool not found")) {
            return "工具不存在，请检查工具名称是否正确";
        } else {
            return "执行失败: " + message;
        }
    }

    /**
     * 评估是否继续
     */
    private boolean evaluateContinuation(ReactThought thought, SseEmitter sseEmitter) throws IOException {
        log.info("第 {} 轮 - 评估是否继续", currentStep);
        String continuePrompt = generateContinuePrompt(thoughts);
        
        thought.setThought("正在评估是否需要继续...");
        sendThought(sseEmitter, thought);
        
        // 使用简化的流式处理
        String continueDecision = streamAIResponse(continuePrompt, "你是一个AI助手");
        
        if (shouldStop(continueDecision)) {
            log.info("第 {} 轮 - 决定停止并生成最终答案", currentStep);
            thought.setDone(true);
            
            thought.setThought("正在生成最终答案...");
            sendThought(sseEmitter, thought);
            
            String finalAnswerPrompt = generateFinalAnswerPrompt(thoughts);
            String finalAnswer = streamAIResponse(finalAnswerPrompt, "你是一个AI助手");
            thought.setFinalAnswer(finalAnswer);
            thought.setMessageType("ai");
            thought.setMessageContent(finalAnswer);
            sendThought(sseEmitter, thought);
            
            return true;
        }
        
        return false;
    }

    /**
     * 处理达到最大步数的情况
     */
    private void handleMaxStepsReached(SseEmitter sseEmitter) throws IOException {
        log.info("达到最大思考次数 {}", MAX_ITERATIONS);
        ReactThought finalThought = new ReactThought();
        finalThought.setThought("已达到最大思考次数限制，正在总结结果...");
        sendThought(sseEmitter, finalThought);
        
        finalThought.setDone(true);
        String finalAnswerPrompt = generateFinalAnswerPrompt(thoughts);
        
        String finalAnswer = streamAIResponse(finalAnswerPrompt, "你是一个AI助手");
        finalThought.setFinalAnswer(finalAnswer);
        sendThought(sseEmitter, finalThought);
    }

    /**
     * 发送错误并完成SSE
     */
    private void sendErrorAndComplete(SseEmitter sseEmitter, String errorMessage) {
        try {
            // 发送错误事件
            sendEvent(sseEmitter, "error", errorMessage);
            
            // 发送错误思考结果
            ReactThought errorThought = new ReactThought();
            errorThought.setThought("错误: " + errorMessage);
            errorThought.setDone(true);
            sendThought(sseEmitter, errorThought);
            
            sseEmitter.complete();
        } catch (IOException e) {
            sseEmitter.completeWithError(e);
        }
    }

    /**
     * 处理错误
     */
    private void handleError(SseEmitter sseEmitter, Exception e) {
        state = ReactState.ERROR;
        log.error("处理过程发生错误", e);
        try {
            // 发送错误事件
            sendEvent(sseEmitter, "error", "处理失败: " + e.getMessage());
            sendErrorAndComplete(sseEmitter, e.getMessage());
        } catch (Exception ex) {
            sseEmitter.completeWithError(ex);
        }
    }

    /**
     * 设置SSE回调
     */
    private void setupSseCallbacks(SseEmitter sseEmitter) {
        // 超时回调
        sseEmitter.onTimeout(() -> {
            this.state = ReactState.ERROR;
            log.warn("SSE连接超时");
            try {
                sendEvent(sseEmitter, "error", "连接超时");
            } catch (IOException e) {
                log.error("发送超时事件失败", e);
            }
            cleanup();
        });
        
        // 完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == ReactState.RUNNING) {
                this.state = ReactState.FINISHED;
            }
            log.info("SSE连接完成");
            cleanup();
        });
        
        // 错误回调
        sseEmitter.onError(ex -> {
            this.state = ReactState.ERROR;
            log.error("SSE连接错误", ex);
            cleanup();
        });
    }

    /**
     * 发送 SSE 数据 - 使用标准格式
     */
    private void sendSSEData(SseEmitter sseEmitter, String eventType, Object data) throws IOException {
        try {
            // 检查 SseEmitter 是否仍然可用
            if (sseEmitter == null) {
                log.warn("SseEmitter 为 null，跳过发送数据");
                return;
            }

            // 检查当前状态
            if (this.state == ReactState.ERROR || this.state == ReactState.FINISHED) {
                log.warn("ReactProcessor 已完成或出错，跳过发送数据");
                return;
            }

            // 将对象转换为JSON字符串
            String jsonData = objectMapper.writeValueAsString(data);

            // 发送标准 SSE 事件
            sseEmitter.send(SseEmitter.event()
                .name(eventType)
                .data(jsonData));

            log.debug("发送SSE数据: event={}, data={}", eventType, jsonData.substring(0, Math.min(100, jsonData.length())));
        } catch (IllegalStateException e) {
            log.warn("SseEmitter 已完成，无法发送数据: {}", e.getMessage());
        } catch (Exception e) {
            log.error("发送SSE数据失败", e);
            throw new IOException("发送SSE数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送思考结果
     */
    private void sendThought(SseEmitter sseEmitter, ReactThought thought) throws IOException {
        sendSSEData(sseEmitter, "message", thought);
    }





    /**
     * 加载相关历史记忆
     */
    private void loadRelevantMemory(String userInput, String sessionId, SseEmitter sseEmitter) throws IOException {
        try {
            sendEvent(sseEmitter, "memory", "正在加载相关记忆...");

            List<ReactThought> relevantHistory = memoryManager.getRelevantHistory(sessionId, userInput, 3);
            if (!relevantHistory.isEmpty()) {
                log.info("加载了 {} 个相关的历史思考", relevantHistory.size());
                sendEvent(sseEmitter, "memory", String.format("找到 %d 个相关的历史记忆", relevantHistory.size()));

                // 将相关历史添加到当前思考过程的开头（作为参考）
                for (ReactThought historyThought : relevantHistory) {
                    ReactThought referenceThought = new ReactThought();
                    referenceThought.setThought("📚 历史参考: " + historyThought.getThought());
                    referenceThought.setAction(historyThought.getAction());
                    referenceThought.setObservation(historyThought.getObservation());
                    referenceThought.setMessageType("reference");
                    sendThought(sseEmitter, referenceThought);
                }
            } else {
                sendEvent(sseEmitter, "memory", "未找到相关历史记忆");
            }
        } catch (Exception e) {
            log.error("加载历史记忆失败", e);
            sendEvent(sseEmitter, "memory", "加载历史记忆失败: " + e.getMessage());
        }
    }

    /**
     * 保存思考过程到记忆和数据库
     */
    private void saveThoughtProcess(String userInput, String sessionId, Long userId) {
        try {
            if (!thoughts.isEmpty()) {
                // 保存到记忆管理器
                memoryManager.saveThoughtProcess(sessionId, userInput, new ArrayList<>(thoughts));
                log.info("已保存思考过程到记忆，会话ID: {}", sessionId);

                // 异步保存AI回复和完整思考过程到数据库
                if (userId != null) {
                    // 获取最终答案
                    String finalAnswer = getFinalAnswerFromThoughts();
                    if (finalAnswer != null && !finalAnswer.trim().isEmpty()) {
                        // 将思考过程转换为JSON字符串
                        String thinkingProcessJson = convertThoughtsToJson();

                        log.info("🔄 开始保存AI消息，会话ID：{}，用户ID：{}，最终答案长度：{}，思考步骤数：{}",
                                sessionId, userId, finalAnswer.length(), thoughts.size());

                        try {
                            // 先尝试同步保存，确保能正常保存
                            messageService.saveAiMessageWithThinking(
                                sessionId,
                                finalAnswer,
                                thinkingProcessJson,
                                1L,
                                "MyAIlogin.svg"
                            );
                            log.info("✅ AI回复和思考过程同步保存成功，会话ID：{}，用户ID：{}", sessionId, userId);

                            // 然后异步保存（如果需要的话）
                            // messageService.saveAiMessageWithThinkingAsync(...);
                        } catch (Exception e) {
                            log.error("❌ AI消息保存失败，会话ID：{}，用户ID：{}，错误：{}", sessionId, userId, e.getMessage(), e);
                        }
                    } else {
                        log.warn("⚠️ 最终答案为空，无法保存AI消息，会话ID：{}", sessionId);
                    }
                } else {
                    log.warn("⚠️ 用户ID为空，无法保存AI消息，会话ID：{}", sessionId);
                }
            }
        } catch (Exception e) {
            log.error("保存思考过程失败", e);
        }
    }

    /**
     * 从思考过程中提取最终答案
     */
    private String getFinalAnswerFromThoughts() {
        if (thoughts.isEmpty()) {
            return null;
        }

        // 查找最后一个有最终答案的思考
        for (int i = thoughts.size() - 1; i >= 0; i--) {
            ReactThought thought = thoughts.get(i);
            if (thought.getFinalAnswer() != null && !thought.getFinalAnswer().trim().isEmpty()) {
                return thought.getFinalAnswer();
            }
        }

        // 如果没有找到最终答案，返回最后一个思考的内容
        ReactThought lastThought = thoughts.get(thoughts.size() - 1);
        if (lastThought.getThought() != null && !lastThought.getThought().trim().isEmpty()) {
            return lastThought.getThought();
        }

        return "处理完成";
    }

    /**
     * 将思考过程转换为JSON字符串
     */
    private String convertThoughtsToJson() {
        try {
            // 创建分步骤的思考过程数据结构
            List<Map<String, Object>> stepList = new ArrayList<>();
            int stepCounter = 1;

            for (ReactThought thought : thoughts) {
                Date timestamp = new Date();

                // 1. 思考步骤
                if (thought.getThought() != null && !thought.getThought().trim().isEmpty()) {
                    Map<String, Object> thinkingStep = new HashMap<>();
                    thinkingStep.put("id", "step-" + stepCounter++);
                    thinkingStep.put("type", "thinking");
                    thinkingStep.put("title", "🤔 思考分析");
                    thinkingStep.put("content", thought.getThought());
                    thinkingStep.put("status", "success");
                    thinkingStep.put("timestamp", timestamp);
                    thinkingStep.put("isTyping", false);
                    thinkingStep.put("displayContent", thought.getThought());
                    stepList.add(thinkingStep);
                }

                // 2. 行动步骤
                if (thought.getAction() != null && !thought.getAction().trim().isEmpty()) {
                    Map<String, Object> actionStep = new HashMap<>();
                    actionStep.put("id", "step-" + stepCounter++);
                    actionStep.put("type", "action");
                    actionStep.put("title", "🔧 执行 " + thought.getAction());

                    String actionContent = "🔧 工具: " + thought.getAction();
                    if (thought.getActionInput() != null && !thought.getActionInput().trim().isEmpty()) {
                        actionContent += "\n📝 参数: " + thought.getActionInput();
                    }

                    actionStep.put("content", actionContent);
                    actionStep.put("status", "success");
                    actionStep.put("timestamp", timestamp);
                    actionStep.put("isTyping", false);
                    actionStep.put("displayContent", actionContent);
                    stepList.add(actionStep);
                }

                // 3. 观察步骤
                if (thought.getObservation() != null && !thought.getObservation().trim().isEmpty()) {
                    Map<String, Object> observationStep = new HashMap<>();
                    observationStep.put("id", "step-" + stepCounter++);
                    observationStep.put("type", "observation");
                    observationStep.put("title", "👀 观察结果");
                    observationStep.put("content", thought.getObservation());
                    observationStep.put("status", "success");
                    observationStep.put("timestamp", timestamp);
                    observationStep.put("isTyping", false);
                    observationStep.put("displayContent", thought.getObservation());
                    stepList.add(observationStep);
                }

                // 4. 最终答案步骤
                if (thought.getFinalAnswer() != null && !thought.getFinalAnswer().trim().isEmpty()) {
                    Map<String, Object> finalStep = new HashMap<>();
                    finalStep.put("id", "step-" + stepCounter++);
                    finalStep.put("type", "final");
                    finalStep.put("title", "✅ 最终答案");

                    // 清理最终答案格式
                    String cleanAnswer = thought.getFinalAnswer();
                    cleanAnswer = cleanAnswer.replaceAll("(?i)^.*Action:\\s*TERMINATE\\s*Action\\s*Input:\\s*", "");
                    cleanAnswer = cleanAnswer.replaceAll("(?i)^工具来结束思考过程并给出回应。\\s*Action:\\s*TERMINATE\\s*Action\\s*Input:\\s*", "");

                    finalStep.put("content", cleanAnswer.trim());
                    finalStep.put("status", "success");
                    finalStep.put("timestamp", timestamp);
                    finalStep.put("isTyping", false);
                    finalStep.put("displayContent", cleanAnswer.trim());
                    stepList.add(finalStep);
                }
            }

            log.info("转换思考过程为JSON，原始思考数量: {}, 转换后步骤数量: {}", thoughts.size(), stepList.size());

            // 使用 Gson 转换为JSON
            return gson.toJson(stepList);
        } catch (Exception e) {
            log.error("转换思考过程为JSON失败", e);
            return "[]"; // 返回空数组
        }
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        thoughts.clear();
        currentStep = 0;
        state = ReactState.IDLE;
        currentAiModelPrompt = REACT_SYSTEM_PROMPT; // 重置为默认prompt
    }

    /**
     * 思考步骤回调接口
     */
    @FunctionalInterface
    public interface ThoughtCallback {
        void onThought(ReactThought thought);
    }



    /**
     * 生成思考提示词
     */
    private String generateThoughtPrompt(String userInput, List<ReactThought> previousThoughts) {
        StringBuilder prompt = new StringBuilder();

        // 添加系统角色和当前任务
        prompt.append("## 当前任务\n");
        prompt.append("用户问题: ").append(userInput).append("\n\n");

        // 添加可用工具信息
        prompt.append("## 可用工具\n");
        prompt.append(toolManager.getAllToolsDescription()).append("\n\n");

        // 添加历史思考过程
        if (!previousThoughts.isEmpty()) {
            prompt.append("## 历史思考过程\n");
            for (int i = 0; i < previousThoughts.size(); i++) {
                ReactThought thought = previousThoughts.get(i);
                prompt.append(String.format("### 第%d轮:\n", i + 1));
                if (thought.getThought() != null && !thought.getThought().isEmpty()) {
                    prompt.append("**思考**: ").append(thought.getThought()).append("\n");
                }
                if (thought.getAction() != null && !thought.getAction().isEmpty()) {
                    prompt.append("**行动**: ").append(thought.getAction()).append("\n");
                }
                if (thought.getObservation() != null && !thought.getObservation().isEmpty()) {
                    prompt.append("**观察**: ").append(thought.getObservation()).append("\n");
                }
                prompt.append("\n");
            }
        }

        // 添加思考指导
        prompt.append("## 请进行下一步思考\n");
        prompt.append("请分析当前情况，确定下一步需要做什么。考虑以下几点:\n");
        prompt.append("1. 用户的问题是否已经得到充分回答？\n");
        prompt.append("2. 还需要获取什么信息？\n");
        prompt.append("3. 应该使用哪个工具来获取所需信息？\n");
        prompt.append("4. 如果信息已经足够，是否可以给出最终答案？\n\n");

        prompt.append("请详细说明你的思考过程。");

        return prompt.toString();
    }

    /**
     * 生成行动提示词
     */
    private String generateActionPrompt(String thought, String toolsDescription) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("## 基于思考选择行动\n\n");

        prompt.append("### 你的思考内容:\n");
        prompt.append(thought).append("\n\n");

        prompt.append("### 可用工具列表:\n");
        prompt.append(toolsDescription).append("\n\n");

        prompt.append("### 行动选择指南:\n");
        prompt.append("请根据你的思考内容，选择最合适的工具来执行下一步行动。\n\n");

        prompt.append("### 输出格式要求:\n");
        prompt.append("请严格按照以下格式输出，不要添加任何其他内容:\n");
        prompt.append("```\n");
        prompt.append("行动：<工具名称>\n");
        prompt.append("参数：<具体参数>\n");
        prompt.append("```\n\n");

        prompt.append("### 重要提醒:\n");
        prompt.append("1. 工具名称必须完全匹配上述工具列表中的名称\n");
        prompt.append("2. 参数必须符合工具的要求格式\n");
        prompt.append("3. 如果需要搜索，请提供具体的搜索关键词\n");
        prompt.append("4. 如果需要计算，请提供完整的表达式\n");
        prompt.append("5. 一次只能选择一个工具执行\n");

        return prompt.toString();
    }

    /**
     * 生成继续思考的提示词
     */
    private String generateContinuePrompt(List<ReactThought> thoughts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("@SystemMessage(\"").append(REACT_SYSTEM_PROMPT).append("\")\n\n");
        
        ReactThought lastThought = thoughts.get(thoughts.size() - 1);
        prompt.append("最新执行结果：\n");
        prompt.append("思考：").append(lastThought.getThought()).append("\n");
        prompt.append("行动：").append(lastThought.getAction()).append("\n");
        prompt.append("观察：").append(lastThought.getObservation()).append("\n\n");
        
        prompt.append("请判断是否已经可以给出最终答案。如果可以，请回复 'FINAL_ANSWER'，否则回复 'CONTINUE'。");
        
        return prompt.toString();
    }

    /**
     * 生成最终答案的提示词
     */
    private String generateFinalAnswerPrompt(List<ReactThought> thoughts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("@SystemMessage(\"").append(REACT_SYSTEM_PROMPT).append("\")\n\n");
        
        prompt.append("基于以下思考过程，请生成一个完整的最终答案：\n\n");
        
        for (ReactThought thought : thoughts) {
            prompt.append("步骤：\n");
            prompt.append("- 思考：").append(thought.getThought()).append("\n");
            prompt.append("- 行动：").append(thought.getAction()).append("\n");
            prompt.append("- 观察：").append(thought.getObservation()).append("\n\n");
        }
        
        prompt.append("请总结以上信息，生成一个全面且有条理的答案。");
        
        return prompt.toString();
    }

    /**
     * 使用重试机制的流式AI响应
     */
    private String streamAIResponseWithRetry(String prompt) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                return streamAIResponse(prompt);
            } catch (Exception e) {
                retries++;
                log.warn("AI响应失败，正在重试 ({}/{}): {}", retries, MAX_RETRIES, e.getMessage());
                if (retries >= MAX_RETRIES) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应失败，已达到最大重试次数: " + e.getMessage());
                }
                try {
                    // 指数退避
                    Thread.sleep(1000L * retries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "重试过程被中断");
                }
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应失败");
    }





    /**
     * 使用流式AI响应
     */
    @SuppressWarnings("deprecation")
    private String streamAIResponse(String prompt) {
        AtomicReference<StringBuilder> responseBuilder = new AtomicReference<>(new StringBuilder());
        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        try {
            List<ChatMessage> messages = List.of(
                SystemMessage.from(currentAiModelPrompt),
                UserMessage.from(prompt)
            );

            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    // 检查状态，如果已经完成或出错，不再处理
                    if (state == ReactState.ERROR || state == ReactState.FINISHED) {
                        log.debug("ReactProcessor 已完成或出错，跳过 onNext 回调");
                        return;
                    }
                    responseBuilder.get().append(token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    completionLatch.countDown();
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                    completionLatch.countDown();
                }
            });

            // 等待响应完成或超时
            if (!completionLatch.await(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应超时");
            }

            // 检查是否有错误
            if (error.get() != null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应错误: " + error.get().getMessage());
            }

            String response = responseBuilder.get().toString().trim();
            if (response.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI返回了空响应");
            }

            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI stream response failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取AI响应失败: " + e.getMessage());
        }
    }

    /**
     * 使用流式AI响应
     */
    @SuppressWarnings("deprecation")
    private String streamAIResponse(String prompt, String modelDescription) {
        AtomicReference<StringBuilder> responseBuilder = new AtomicReference<>(new StringBuilder());
        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        
        try {
            List<ChatMessage> messages = List.of(
                SystemMessage.from(modelDescription),
                UserMessage.from(prompt)
            );

            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    // 检查状态，如果已经完成或出错，不再处理
                    if (state == ReactState.ERROR || state == ReactState.FINISHED) {
                        log.debug("ReactProcessor 已完成或出错，跳过 onNext 回调");
                        return;
                    }
                    responseBuilder.get().append(token);
                    // 每收到一个部分响应就更新当前的思考内容
                    ReactThought currentThought = new ReactThought();
                    currentThought.setThought(responseBuilder.get().toString().trim());
                    // thoughtCallback.onThought(currentThought); // This line was removed as per the new_code
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    completionLatch.countDown();
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                    completionLatch.countDown();
                }
            });
            
            // 等待响应完成或超时
            if (!completionLatch.await(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应超时");
            }

            // 检查是否有错误
            if (error.get() != null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应错误: " + error.get().getMessage());
            }

            String response = responseBuilder.get().toString().trim();
            if (response.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI返回了空响应");
            }

            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI stream response failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取AI响应失败: " + e.getMessage());
        }
    }



    /**
     * 解析行动计划
     */
    private String[] parseActionPlan(String actionPlan) {
        if (actionPlan == null || actionPlan.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未找到有效的行动计划");
        }

        log.info("解析行动计划: {}", actionPlan);

        try {
            String[] result = new String[2];
            String cleanPlan = actionPlan.trim();

            // 格式1: 标准格式 "行动：XXX\n参数：YYY"
            Pattern pattern1 = Pattern.compile("行动[：:]\\s*([^\\n]+)\\s*\\n?\\s*参数[：:]\\s*(.+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = pattern1.matcher(cleanPlan);
            if (matcher1.find()) {
                result[0] = cleanToolName(matcher1.group(1).trim());
                result[1] = matcher1.group(2).trim();
                log.info("使用格式1解析成功: 工具={}, 参数={}", result[0], result[1]);
                return result;
            }

            // 格式2: 英文格式 "Action: XXX\nInput: YYY"
            Pattern pattern2 = Pattern.compile("Action[：:]\\s*([^\\n]+)\\s*\\n?\\s*Input[：:]\\s*(.+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(cleanPlan);
            if (matcher2.find()) {
                result[0] = cleanToolName(matcher2.group(1).trim());
                result[1] = matcher2.group(2).trim();
                log.info("使用格式2解析成功: 工具={}, 参数={}", result[0], result[1]);
                return result;
            }

            // 格式3: 代码块格式
            Pattern pattern3 = Pattern.compile("```\\s*行动[：:]\\s*([^\\n]+)\\s*\\n?\\s*参数[：:]\\s*([^`]+)\\s*```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher3 = pattern3.matcher(cleanPlan);
            if (matcher3.find()) {
                result[0] = cleanToolName(matcher3.group(1).trim());
                result[1] = matcher3.group(2).trim();
                log.info("使用格式3解析成功: 工具={}, 参数={}", result[0], result[1]);
                return result;
            }

            // 格式4: 管道分隔符格式 "XXX|YYY"
            String[] parts = cleanPlan.split("\\|");
            if (parts.length >= 2) {
                result[0] = cleanToolName(parts[0].trim());
                result[1] = String.join("|", Arrays.copyOfRange(parts, 1, parts.length)).trim();
                log.info("使用格式4解析成功: 工具={}, 参数={}", result[0], result[1]);
                return result;
            }

            // 格式5: 智能匹配工具名称
            result = intelligentToolMatching(cleanPlan);
            if (result[0] != null) {
                log.info("使用智能匹配解析成功: 工具={}, 参数={}", result[0], result[1]);
                return result;
            }

            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无法解析行动计划: " + actionPlan);
        } catch (Exception e) {
            log.error("解析行动计划失败", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "解析行动计划失败: " + e.getMessage());
        }
    }

    /**
     * 清理工具名称
     */
    private String cleanToolName(String toolName) {
        if (toolName == null) return null;

        // 去除引号、括号等
        String cleaned = toolName.replaceAll("[\"'`()\\[\\]{}]", "").trim();

        // 工具名称映射
        Map<String, String> toolMapping = Map.of(
            "搜索", "WEB_SEARCH",
            "网络搜索", "WEB_SEARCH",
            "计算", "CALCULATOR",
            "计算器", "CALCULATOR",
            "代码执行", "CODE_EXECUTOR",
            "执行代码", "CODE_EXECUTOR",
            "文本处理", "TEXT_PROCESSOR",
            "时间", "DATETIME",
            "日期", "DATETIME"
        );

        // 首先尝试直接匹配
        if (toolManager.isToolAvailable(cleaned)) {
            return cleaned;
        }

        // 尝试映射匹配
        for (Map.Entry<String, String> entry : toolMapping.entrySet()) {
            if (cleaned.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return cleaned;
    }

    /**
     * 智能工具匹配
     */
    private String[] intelligentToolMatching(String text) {
        String[] result = new String[2];

        // 获取所有可用工具名称
        List<String> toolNames = toolManager.getAllToolNames();

        // 尝试在文本中找到工具名称
        for (String toolName : toolNames) {
            if (text.toUpperCase().contains(toolName)) {
                result[0] = toolName;
                // 提取工具名后的内容作为参数
                int index = text.toUpperCase().indexOf(toolName) + toolName.length();
                if (index < text.length()) {
                    result[1] = text.substring(index).trim()
                        .replaceAll("^[：:,，\\s]+", ""); // 去除开头的标点和空白
                } else {
                    result[1] = "";
                }
                return result;
            }
        }

        // 基于关键词智能匹配
        if (text.contains("搜索") || text.contains("查找") || text.contains("search")) {
            result[0] = "WEB_SEARCH";
            result[1] = extractSearchQuery(text);
        } else if (text.contains("计算") || text.contains("算") || text.contains("math")) {
            result[0] = "CALCULATOR";
            result[1] = extractCalculationExpression(text);
        } else if (text.contains("代码") || text.contains("执行") || text.contains("code")) {
            result[0] = "CODE_EXECUTOR";
            result[1] = extractCodeContent(text);
        } else if (text.contains("文本") || text.contains("处理") || text.contains("text")) {
            result[0] = "TEXT_PROCESSOR";
            result[1] = extractTextProcessingParams(text);
        } else if (text.contains("时间") || text.contains("日期") || text.contains("date")) {
            result[0] = "DATETIME";
            result[1] = extractDateTimeParams(text);
        }

        return result;
    }

    /**
     * 提取搜索查询
     */
    private String extractSearchQuery(String text) {
        // 尝试提取引号内的内容
        Pattern quotedPattern = Pattern.compile("[\"'](.*?)[\"']");
        Matcher quotedMatcher = quotedPattern.matcher(text);
        if (quotedMatcher.find()) {
            return quotedMatcher.group(1);
        }

        // 提取"搜索"后的内容
        Pattern searchPattern = Pattern.compile("搜索[：:]?\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher searchMatcher = searchPattern.matcher(text);
        if (searchMatcher.find()) {
            return searchMatcher.group(1).trim();
        }

        return text;
    }

    /**
     * 提取计算表达式
     */
    private String extractCalculationExpression(String text) {
        // 尝试提取数学表达式
        Pattern mathPattern = Pattern.compile("([0-9+\\-*/().\\s^]+)");
        Matcher mathMatcher = mathPattern.matcher(text);
        if (mathMatcher.find()) {
            return mathMatcher.group(1).trim();
        }

        return text;
    }

    /**
     * 提取代码内容
     */
    private String extractCodeContent(String text) {
        // 尝试提取代码块
        Pattern codePattern = Pattern.compile("```(?:javascript|js)?\\s*([^`]+)\\s*```", Pattern.CASE_INSENSITIVE);
        Matcher codeMatcher = codePattern.matcher(text);
        if (codeMatcher.find()) {
            return codeMatcher.group(1).trim();
        }

        return text;
    }

    /**
     * 提取文本处理参数
     */
    private String extractTextProcessingParams(String text) {
        // 默认返回原文本，让工具自己解析
        return text;
    }

    /**
     * 提取日期时间参数
     */
    private String extractDateTimeParams(String text) {
        // 默认返回原文本，让工具自己解析
        return text;
    }

    /**
     * 执行行动
     */
    private String executeAction(String action, String input) {
        log.info("准备执行行动 - 工具: {}, 输入: {}", action, input);
        if (!toolManager.isToolAvailable(action)) {
            String errorMsg = "工具不可用: " + action;
            log.error(errorMsg);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, errorMsg);
        }
        try {
            String result = toolManager.executeTool(action, input);
            log.info("行动执行完成 - 结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("行动执行失败", e);
            throw e;
        }
    }

    /**
     * 判断是否应该停止思考
     */
    private boolean shouldStop(String decision) {
        return "FINAL_ANSWER".equalsIgnoreCase(decision.trim());
    }

    /**
     * 生成最终答案
     */
    private String generateFinalAnswer(List<ReactThought> thoughts, String modelDescription) {
        StringBuilder summaryPrompt = new StringBuilder();
        summaryPrompt.append("@SystemMessage(\"").append(REACT_SYSTEM_PROMPT).append("\")\n\n");
        
        summaryPrompt.append("基于以下思考过程，请生成一个完整的最终答案：\n\n");
        
        for (ReactThought thought : thoughts) {
            summaryPrompt.append("步骤：\n");
            summaryPrompt.append("- 思考：").append(thought.getThought()).append("\n");
            summaryPrompt.append("- 行动：").append(thought.getAction()).append("\n");
            summaryPrompt.append("- 观察：").append(thought.getObservation()).append("\n\n");
        }
        
        return streamAIResponse(summaryPrompt.toString());
    }

    /**
     * ReAct状态枚举
     */
    public enum ReactState {
        IDLE,       // 空闲
        RUNNING,    // 运行中
        FINISHED,   // 已完成
        ERROR       // 错误
    }
} 