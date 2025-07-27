package com.myAI.myAI.mapper;

import com.myAI.myAI.models.entity.AiModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * AI模型Mapper接口
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModel> {

    /**
     * 根据分类获取启用的模型列表
     */
    @Select("SELECT * FROM aiModel WHERE category = #{category} AND isActive = 1 AND isDeleted = 0 ORDER BY sortOrder DESC, createTime DESC")
    List<AiModel> selectByCategory(@Param("category") String category);

    /**
     * 获取推荐模型列表
     */
    @Select("SELECT * FROM aiModel WHERE isOfficial = 1 AND isActive = 1 AND isDeleted = 0 ORDER BY sortOrder DESC, usageCount DESC, createTime DESC")
    List<AiModel> selectRecommendModels();

    /**
     * 增加模型使用次数
     */
    @Update("UPDATE aiModel SET usageCount = usageCount + 1 WHERE id = #{id}")
    int incrementUsageCount(@Param("id") Long id);

    /**
     * 获取热门模型（按使用次数排序）
     */
    @Select("SELECT * FROM aiModel WHERE isActive = 1 AND isDeleted = 0 ORDER BY usageCount DESC, createTime DESC LIMIT #{limit}")
    List<AiModel> selectPopularModels(@Param("limit") int limit);
}
