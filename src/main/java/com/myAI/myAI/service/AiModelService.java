package com.myAI.myAI.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myAI.myAI.models.dto.aimodel.AiModelAddRequest;
import com.myAI.myAI.models.dto.aimodel.AiModelQueryRequest;
import com.myAI.myAI.models.dto.aimodel.AiModelUpdateRequest;
import com.myAI.myAI.models.entity.AiModel;
import com.myAI.myAI.models.entity.AiModelCategory;
import com.myAI.myAI.models.vo.AiModelVO;

import java.util.List;

/**
 * AI模型服务接口
 */
public interface AiModelService extends IService<AiModel> {

    /**
     * 添加AI模型
     */
    Long addAiModel(AiModelAddRequest addRequest, Long userId);

    /**
     * 更新AI模型
     */
    boolean updateAiModel(AiModelUpdateRequest updateRequest, Long userId);

    /**
     * 删除AI模型
     */
    boolean deleteAiModel(Long id, Long userId);

    /**
     * 根据ID获取AI模型
     */
    AiModel getAiModelById(Long id);

    /**
     * 根据ID获取AI模型VO
     */
    AiModelVO getAiModelVOById(Long id);

    /**
     * 分页查询AI模型
     */
    IPage<AiModelVO> listAiModelVOByPage(AiModelQueryRequest queryRequest);

    /**
     * 根据分类获取AI模型列表
     */
    List<AiModelVO> listAiModelByCategory(String category);

    /**
     * 获取推荐AI模型列表
     */
    List<AiModelVO> listRecommendAiModels();

    /**
     * 获取热门AI模型列表
     */
    List<AiModelVO> listPopularAiModels(int limit);

    /**
     * 增加模型使用次数
     */
    boolean incrementUsageCount(Long id);

    /**
     * 获取所有分类
     */
    List<AiModelCategory> listAllCategories();

    /**
     * 获取启用的分类
     */
    List<AiModelCategory> listActiveCategories();

    /**
     * 实体转VO
     */
    AiModelVO getAiModelVO(AiModel aiModel);

    /**
     * 实体列表转VO列表
     */
    List<AiModelVO> getAiModelVOList(List<AiModel> aiModelList);
}
