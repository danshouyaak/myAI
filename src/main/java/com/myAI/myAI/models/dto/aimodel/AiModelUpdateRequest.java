package com.myAI.myAI.models.dto.aimodel;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * AI模型更新请求
 */
@Data
public class AiModelUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    @NotNull(message = "模型ID不能为空")
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
     * 模型标签
     */
    private List<String> tagList;

    /**
     * 是否官方模型
     */
    private Integer isOfficial;

    /**
     * 是否启用
     */
    private Integer isActive;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
