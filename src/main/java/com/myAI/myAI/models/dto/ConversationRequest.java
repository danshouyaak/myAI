package com.myAI.myAI.models.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话请求
 */
@Data
public class ConversationRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;
    private String userId;
    private String aiId;
    private String conversationName;
}
