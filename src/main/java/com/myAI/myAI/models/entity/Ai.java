package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName ai
 */
@TableName(value ="ai")
@Data
public class Ai implements Serializable {
    private Long aiId;

    private String aiName;

    private String aiUrl;

    private String modelVersion;

    private String description;

    private Date createdTime;

    private Date updatedTime;

    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}