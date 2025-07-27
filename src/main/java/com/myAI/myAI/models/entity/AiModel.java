package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * AI模型实体类
 * @TableName aiModel
 */
@TableName(value = "aiModel")
@Data
public class AiModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 模型图标URL
     */
    private String icon;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 模型的系统提示词
     */
    private String prompt;

    /**
     * 模型来源/作者
     */
    private String source;

    /**
     * 模型标签（JSON数组）
     */
    private String tags;

    /**
     * 是否官方模型：0-否，1-是
     */
    private Integer isOfficial;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isActive;

    /**
     * 排序权重，数值越大越靠前
     */
    private Integer sortOrder;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除：0-否，1-是
     */
    @TableLogic
    private Integer isDeleted;

    // 非数据库字段，用于前端展示
    @TableField(exist = false)
    private List<String> tagList;
}
