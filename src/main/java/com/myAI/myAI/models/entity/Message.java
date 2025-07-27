package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 消息实体类
 * @TableName message
 */
@TableName(value = "message")
@Data
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long messageId;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * AI ID
     */
    private Long aiId;

    /**
     * AI 头像URL
     */
    private String aiUrl;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 思考过程（JSON格式存储）
     */
    // @TableField(exist = false) // 字段添加成功后请移除此注解
    private String thinkingProcess;

    /**
     * 消息类型：user/ai
     */
    private String messageType;

    /**
     * 发送时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date sendTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
}