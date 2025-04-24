package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户处理时间格式
 */
@Data
public class MessageFormat implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long messageId;
    private String conversationId;
    private String messageContent;
    private Object messageType;
    private String sendTime;
    private Long aiId;
    private String aiUrl;
    @TableLogic
    private Integer isDeleted;
}
