package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * @TableName message
 */
@TableName(value ="message")
@Data
public class Message implements Serializable {
    private Long messageId;

    private String conversationId;

    private String messageContent;

    @Value("active")
    private Object messageType;

    private Date sendTime;

    @TableLogic
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}