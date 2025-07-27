package com.myAI.myAI.models.dto.aimodel;

import lombok.Data;

import java.io.Serializable;

/**
 * AI模型查询请求
 */
@Data
public class AiModelQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    private Long id;

    /**
     * 模型名称（模糊搜索）
     */
    private String name;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 是否官方模型
     */
    private Integer isOfficial;

    /**
     * 是否启用
     */
    private Integer isActive;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 搜索关键词（搜索名称和描述）
     */
    private String keyword;

    /**
     * 标签过滤
     */
    private String tag;

    /**
     * 排序字段：usage_count/create_time/sort_order
     */
    private String sortField = "sort_order";

    /**
     * 排序方向：asc/desc
     */
    private String sortOrder = "desc";

    /**
     * 页码
     */
    private Integer current = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 10;
}
