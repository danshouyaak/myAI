package com.myAI.myAI.ai.react;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ReAct错误处理器
 * 提供智能的错误恢复策略
 */
@Component
@Slf4j
public class ReactErrorHandler {
    
    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        TOOL_NOT_FOUND,         // 工具不存在
        TOOL_UNAVAILABLE,       // 工具不可用
        INVALID_PARAMETERS,     // 参数无效
        EXECUTION_TIMEOUT,      // 执行超时
        CONNECTION_ERROR,       // 连接错误
        PARSING_ERROR,          // 解析错误
        UNKNOWN_ERROR          // 未知错误
    }
    
    /**
     * 错误恢复策略
     */
    private final Map<ErrorType, Function<String, String>> recoveryStrategies;
    
    public ReactErrorHandler() {
        this.recoveryStrategies = new HashMap<>();
        initializeRecoveryStrategies();
    }
    
    /**
     * 初始化恢复策略
     */
    private void initializeRecoveryStrategies() {
        // 工具不存在的恢复策略
        recoveryStrategies.put(ErrorType.TOOL_NOT_FOUND, this::handleToolNotFound);
        
        // 工具不可用的恢复策略
        recoveryStrategies.put(ErrorType.TOOL_UNAVAILABLE, this::handleToolUnavailable);
        
        // 参数无效的恢复策略
        recoveryStrategies.put(ErrorType.INVALID_PARAMETERS, this::handleInvalidParameters);
        
        // 执行超时的恢复策略
        recoveryStrategies.put(ErrorType.EXECUTION_TIMEOUT, this::handleExecutionTimeout);
        
        // 连接错误的恢复策略
        recoveryStrategies.put(ErrorType.CONNECTION_ERROR, this::handleConnectionError);
        
        // 解析错误的恢复策略
        recoveryStrategies.put(ErrorType.PARSING_ERROR, this::handleParsingError);
        
        // 未知错误的恢复策略
        recoveryStrategies.put(ErrorType.UNKNOWN_ERROR, this::handleUnknownError);
    }
    
    /**
     * 处理错误并提供恢复建议
     */
    public String handleError(Exception exception, String context) {
        ErrorType errorType = classifyError(exception);
        log.warn("检测到错误类型: {}, 上下文: {}", errorType, context);
        
        Function<String, String> strategy = recoveryStrategies.get(errorType);
        if (strategy != null) {
            return strategy.apply(context);
        }
        
        return handleUnknownError(context);
    }
    
    /**
     * 分类错误类型
     */
    private ErrorType classifyError(Exception exception) {
        String message = exception.getMessage().toLowerCase();
        
        if (message.contains("tool not found") || message.contains("工具不存在")) {
            return ErrorType.TOOL_NOT_FOUND;
        } else if (message.contains("tool not available") || message.contains("工具不可用")) {
            return ErrorType.TOOL_UNAVAILABLE;
        } else if (message.contains("invalid parameter") || message.contains("参数无效")) {
            return ErrorType.INVALID_PARAMETERS;
        } else if (message.contains("timeout") || message.contains("超时")) {
            return ErrorType.EXECUTION_TIMEOUT;
        } else if (message.contains("connection") || message.contains("连接")) {
            return ErrorType.CONNECTION_ERROR;
        } else if (message.contains("parse") || message.contains("解析")) {
            return ErrorType.PARSING_ERROR;
        } else {
            return ErrorType.UNKNOWN_ERROR;
        }
    }
    
    /**
     * 处理工具不存在错误
     */
    private String handleToolNotFound(String context) {
        return "工具不存在错误。建议:\n" +
               "1. 检查工具名称是否正确\n" +
               "2. 使用可用工具列表中的工具\n" +
               "3. 尝试使用相似功能的其他工具\n" +
               "请重新选择一个有效的工具。";
    }
    
    /**
     * 处理工具不可用错误
     */
    private String handleToolUnavailable(String context) {
        return "工具暂时不可用。建议:\n" +
               "1. 稍后重试\n" +
               "2. 使用备用工具\n" +
               "3. 检查工具依赖服务是否正常\n" +
               "请选择其他可用的工具继续任务。";
    }
    
    /**
     * 处理参数无效错误
     */
    private String handleInvalidParameters(String context) {
        return "参数格式错误。建议:\n" +
               "1. 检查参数格式是否符合工具要求\n" +
               "2. 确保必需参数都已提供\n" +
               "3. 参考工具的参数说明\n" +
               "请修正参数后重试。";
    }
    
    /**
     * 处理执行超时错误
     */
    private String handleExecutionTimeout(String context) {
        return "执行超时。建议:\n" +
               "1. 简化操作或减少数据量\n" +
               "2. 分步骤执行复杂任务\n" +
               "3. 检查网络连接状态\n" +
               "请尝试更简单的操作或稍后重试。";
    }
    
    /**
     * 处理连接错误
     */
    private String handleConnectionError(String context) {
        return "连接错误。建议:\n" +
               "1. 检查网络连接\n" +
               "2. 确认服务是否正常运行\n" +
               "3. 稍后重试\n" +
               "4. 使用离线工具作为替代\n" +
               "请检查连接状态后重试。";
    }
    
    /**
     * 处理解析错误
     */
    private String handleParsingError(String context) {
        return "解析错误。建议:\n" +
               "1. 检查输入格式是否正确\n" +
               "2. 确保数据完整性\n" +
               "3. 使用标准格式重新输入\n" +
               "请修正输入格式后重试。";
    }
    
    /**
     * 处理未知错误
     */
    private String handleUnknownError(String context) {
        return "遇到未知错误。建议:\n" +
               "1. 尝试不同的方法\n" +
               "2. 简化操作步骤\n" +
               "3. 使用其他工具\n" +
               "4. 如果问题持续，请联系管理员\n" +
               "请尝试其他方法继续任务。";
    }
    
    /**
     * 生成错误恢复提示
     */
    public String generateRecoveryPrompt(Exception exception, String originalAction, String originalInput) {
        ErrorType errorType = classifyError(exception);
        String recoveryAdvice = handleError(exception, originalAction + "|" + originalInput);
        
        return String.format(
            "执行失败分析:\n" +
            "- 原始行动: %s\n" +
            "- 原始参数: %s\n" +
            "- 错误类型: %s\n" +
            "- 错误信息: %s\n\n" +
            "恢复建议:\n%s\n\n" +
            "请基于以上分析，重新思考并选择合适的行动。",
            originalAction,
            originalInput,
            errorType,
            exception.getMessage(),
            recoveryAdvice
        );
    }
    
    /**
     * 判断是否应该重试
     */
    public boolean shouldRetry(Exception exception, int currentRetryCount, int maxRetries) {
        if (currentRetryCount >= maxRetries) {
            return false;
        }
        
        ErrorType errorType = classifyError(exception);
        
        // 某些错误类型适合重试
        switch (errorType) {
            case EXECUTION_TIMEOUT:
            case CONNECTION_ERROR:
                return true;
            case TOOL_UNAVAILABLE:
                return currentRetryCount < 2; // 最多重试2次
            default:
                return false;
        }
    }
    
    /**
     * 计算重试延迟时间（毫秒）
     */
    public long calculateRetryDelay(int retryCount) {
        // 指数退避策略：1秒、2秒、4秒...
        return Math.min(1000L * (1L << retryCount), 10000L); // 最大10秒
    }
}
