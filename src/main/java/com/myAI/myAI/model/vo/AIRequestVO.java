package com.myAI.myAI.model.vo;

import lombok.Data;

@Data
public class AIRequestVO {
    /**
     * 模型id
     */
    private String modelId;
    /**
     * 聊天内容
     */
    private String content;
}
