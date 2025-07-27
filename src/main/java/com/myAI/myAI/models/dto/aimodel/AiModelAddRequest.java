package com.myAI.myAI.models.dto.aimodel;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * AI模型添加请求
 */
@Data
public class AiModelAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
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
    @NotBlank(message = "模型分类不能为空")
    private String category;

    /**
     * 模型的系统提示词
     */
    @NotBlank(message = "系统提示词不能为空")
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
    private Integer isOfficial = 0;

    /**
     * 排序权重
     */
    private Integer sortOrder = 0;
}
