package com.myAI.myAI.models.vo;

import lombok.Data;

@Data
public class AIRequestVO {
    /**
     * 模型id
     */
    private long modelId;
    /**
     * 聊天内容
     */
    private String content;
}
