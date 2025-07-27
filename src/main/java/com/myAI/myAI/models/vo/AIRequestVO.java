package com.myAI.myAI.models.vo;

import lombok.Data;

@Data
public class AIRequestVO {
    /**
     * 消息内容
     */
    private String message;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 模型ID
     */
    private Long modelId;
}
