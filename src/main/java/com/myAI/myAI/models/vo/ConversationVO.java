package com.myAI.myAI.models.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会话视图对象
 */
@Data
public class ConversationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * AI模型ID
     */
    private String aiId;

    /**
     * AI模型名称
     */
    private String aiName;

    /**
     * AI模型头像
     */
    private String aiIcon;

    /**
     * 会话描述
     */
    private String description;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 会话状态
     */
    private String conversationState;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;
}
