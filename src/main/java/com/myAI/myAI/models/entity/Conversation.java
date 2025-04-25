package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName conversation
 */
@TableName(value ="conversation")
@Data
public class Conversation implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private String conversationId;

    private String userId;

    private String aiId;

    private Date startTime;

    private Date endTime;

    private Object conversationState;

    @TableLogic
    private Integer isDeleted;

    private Date createdTime;

    private Date updatedTime;

    private String description;

    private static final long serialVersionUID = 1L;
}