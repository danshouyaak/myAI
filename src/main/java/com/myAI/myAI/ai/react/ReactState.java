package com.myAI.myAI.ai.react;

/**
 * ReAct 处理器状态枚举
 */
public enum ReactState {
    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 初始化状态
     */
    INITIALIZED,

    /**
     * 正在运行
     */
    RUNNING,

    /**
     * 正在处理
     */
    PROCESSING,

    /**
     * 已完成
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}
