package com.myAI.myAI.ai.react;

import lombok.Data;

/**
 * ReAct思维模型
 * 用于存储AI的思考过程
 */
@Data
public class ReactThought {
    /**
     * 思考内容
     */
    private String thought;
    
    /**
     * 行动计划
     */
    private String action;
    
    /**
     * 行动参数
     */
    private String actionInput;
    
    /**
     * 观察结果
     */
    private String observation;
    
    /**
     * 最终答案
     */
    private String finalAnswer;
    
    /**
     * 是否完成
     */
    private boolean done;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息时间
     */
    private String messageTime;

    /**
     * 是否为流式数据
     */
    private boolean streaming = false;

    /**
     * 流式数据是否完成
     */
    private boolean streamingComplete = false;
} 