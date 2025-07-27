package com.myAI.myAI.ai.react;

/**
 * ReAct工具接口
 * 所有可用的工具都需要实现此接口
 */
public interface ReactTool {
    /**
     * 获取工具名称
     */
    String getName();

    /**
     * 获取工具描述
     */
    String getDescription();

    /**
     * 执行工具
     * @param input 输入参数
     * @return 执行结果
     */
    String execute(String input);

    /**
     * 获取工具的参数说明
     */
    String getParameterDescription();

    /**
     * 是否可用
     */
    boolean isAvailable();
} 