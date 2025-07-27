package com.myAI.myAI.models.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * AI模型视图对象
 */
@Data
public class AiModelVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
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
     * 模型分类名称
     */
    private String categoryName;

    /**
     * 模型来源/作者
     */
    private String source;

    /**
     * 模型标签
     */
    private List<String> tagList;

    /**
     * 是否官方模型
     */
    private Integer isOfficial;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
