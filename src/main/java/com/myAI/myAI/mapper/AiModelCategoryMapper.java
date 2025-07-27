package com.myAI.myAI.mapper;

import com.myAI.myAI.models.entity.AiModelCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI模型分类Mapper接口
 */
@Mapper
public interface AiModelCategoryMapper extends BaseMapper<AiModelCategory> {

    /**
     * 获取启用的分类列表
     */
    @Select("SELECT * FROM aiModelCategory WHERE isActive = 1 ORDER BY sortOrder DESC, createTime ASC")
    List<AiModelCategory> selectActiveCategories();
}
