package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志
 */
@TableName(value = "operation_log")
@Data
public class OperationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("userId")
    private Long userId;

    /**
     * 操作类型（例如：LOGIN, LOGOUT, CHAT, UPDATE_PROFILE等）
     */
    @TableField("operationType")
    private String operationType;

    /**
     * 操作描述
     */
    @TableField("description")
    private String description;

    /**
     * 操作结果（成功/失败）
     */
    @TableField("success")
    private Boolean success;

    /**
     * 操作IP地址
     */
    @TableField("ipAddress")
    private String ipAddress;

    /**
     * 操作时间
     */
    @TableField("operationTime")
    private Date operationTime;

    /**
     * 相关业务ID（如会话ID、消息ID等）
     */
    @TableField("businessId")
    private String businessId;

    /**
     * 请求参数
     */
    @TableField("requestParams")
    private String requestParams;

    /**
     * 错误信息（如果操作失败）
     */
    @TableField("errorMessage")
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField("isDeleted")
    @TableLogic
    private Integer isDeleted;
} 