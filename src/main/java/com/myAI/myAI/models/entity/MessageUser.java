package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MessageUser implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long messageId;
    private String conversationId;
    private String messageContent;
    private Object messageType;
    private Date sendTime;
    @TableLogic
    private Integer isDeleted;
    private Long aiId;
    private String aiUrl;

    private Long userId;
}
