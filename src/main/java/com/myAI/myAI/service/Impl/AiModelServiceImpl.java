package com.myAI.myAI.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.mapper.AiModelCategoryMapper;
import com.myAI.myAI.mapper.AiModelMapper;
import com.myAI.myAI.models.dto.aimodel.AiModelAddRequest;
import com.myAI.myAI.models.dto.aimodel.AiModelQueryRequest;
import com.myAI.myAI.models.dto.aimodel.AiModelUpdateRequest;
import com.myAI.myAI.models.entity.AiModel;
import com.myAI.myAI.models.entity.AiModelCategory;
import com.myAI.myAI.models.vo.AiModelVO;
import com.myAI.myAI.service.AiModelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI模型服务实现类
 */
@Service
@Slf4j
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements AiModelService {

    @Resource
    private AiModelMapper aiModelMapper;

    @Resource
    private AiModelCategoryMapper aiModelCategoryMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Long addAiModel(AiModelAddRequest addRequest, Long userId) {
        if (addRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = new AiModel();
        BeanUtils.copyProperties(addRequest, aiModel);
        
        // 处理标签
        if (addRequest.getTagList() != null && !addRequest.getTagList().isEmpty()) {
            try {
                aiModel.setTags(objectMapper.writeValueAsString(addRequest.getTagList()));
            } catch (Exception e) {
                log.error("标签序列化失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "标签格式错误");
            }
        }
        
        aiModel.setCreatorId(userId);
        aiModel.setIsActive(1);
        aiModel.setUsageCount(0L);

        boolean result = this.save(aiModel);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        return aiModel.getId();
    }

    @Override
    public boolean updateAiModel(AiModelUpdateRequest updateRequest, Long userId) {
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel oldAiModel = this.getById(updateRequest.getId());
        if (oldAiModel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 检查权限（只有创建者或管理员可以修改）
        if (!oldAiModel.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        AiModel aiModel = new AiModel();
        BeanUtils.copyProperties(updateRequest, aiModel);
        
        // 处理标签
        if (updateRequest.getTagList() != null) {
            try {
                aiModel.setTags(objectMapper.writeValueAsString(updateRequest.getTagList()));
            } catch (Exception e) {
                log.error("标签序列化失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "标签格式错误");
            }
        }

        return this.updateById(aiModel);
    }

    @Override
    public boolean deleteAiModel(Long id, Long userId) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = this.getById(id);
        if (aiModel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 检查权限
        if (!aiModel.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return this.removeById(id);
    }

    @Override
    public AiModel getAiModelById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.getById(id);
    }

    @Override
    public AiModelVO getAiModelVOById(Long id) {
        AiModel aiModel = this.getAiModelById(id);
        if (aiModel == null) {
            return null;
        }
        return this.getAiModelVO(aiModel);
    }

    @Override
    public IPage<AiModelVO> listAiModelVOByPage(AiModelQueryRequest queryRequest) {
        if (queryRequest == null) {
            queryRequest = new AiModelQueryRequest();
        }

        QueryWrapper<AiModel> queryWrapper = new QueryWrapper<>();
        
        // 构建查询条件
        if (StringUtils.isNotBlank(queryRequest.getName())) {
            queryWrapper.like("name", queryRequest.getName());
        }
        if (StringUtils.isNotBlank(queryRequest.getCategory())) {
            queryWrapper.eq("category", queryRequest.getCategory());
        }
        if (queryRequest.getIsOfficial() != null) {
            queryWrapper.eq("is_official", queryRequest.getIsOfficial());
        }
        if (queryRequest.getIsActive() != null) {
            queryWrapper.eq("is_active", queryRequest.getIsActive());
        }
        if (queryRequest.getCreatorId() != null) {
            queryWrapper.eq("creator_id", queryRequest.getCreatorId());
        }
        if (StringUtils.isNotBlank(queryRequest.getKeyword())) {
            final String keyword = queryRequest.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                .like("name", keyword)
                .or()
                .like("description", keyword)
            );
        }

        // 排序
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = "asc".equals(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            queryWrapper.orderByDesc("sort_order", "create_time");
        }

        Page<AiModel> page = new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize());
        IPage<AiModel> aiModelPage = this.page(page, queryWrapper);
        
        // 转换为VO
        List<AiModelVO> aiModelVOList = this.getAiModelVOList(aiModelPage.getRecords());
        
        Page<AiModelVO> aiModelVOPage = new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize(), aiModelPage.getTotal());
        aiModelVOPage.setRecords(aiModelVOList);
        
        return aiModelVOPage;
    }

    @Override
    public List<AiModelVO> listAiModelByCategory(String category) {
        if (StringUtils.isBlank(category)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<AiModel> aiModelList = aiModelMapper.selectByCategory(category);
        return this.getAiModelVOList(aiModelList);
    }

    @Override
    public List<AiModelVO> listRecommendAiModels() {
        List<AiModel> aiModelList = aiModelMapper.selectRecommendModels();
        return this.getAiModelVOList(aiModelList);
    }

    @Override
    public List<AiModelVO> listPopularAiModels(int limit) {
        List<AiModel> aiModelList = aiModelMapper.selectPopularModels(limit);
        return this.getAiModelVOList(aiModelList);
    }

    @Override
    public boolean incrementUsageCount(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return aiModelMapper.incrementUsageCount(id) > 0;
    }

    @Override
    public List<AiModelCategory> listAllCategories() {
        return aiModelCategoryMapper.selectList(null);
    }

    @Override
    public List<AiModelCategory> listActiveCategories() {
        return aiModelCategoryMapper.selectActiveCategories();
    }

    @Override
    public AiModelVO getAiModelVO(AiModel aiModel) {
        if (aiModel == null) {
            return null;
        }

        AiModelVO aiModelVO = new AiModelVO();
        BeanUtils.copyProperties(aiModel, aiModelVO);
        
        // 处理标签
        if (StringUtils.isNotBlank(aiModel.getTags())) {
            try {
                List<String> tagList = objectMapper.readValue(aiModel.getTags(), new TypeReference<List<String>>() {});
                aiModelVO.setTagList(tagList);
            } catch (Exception e) {
                log.error("标签反序列化失败", e);
                aiModelVO.setTagList(new ArrayList<>());
            }
        } else {
            aiModelVO.setTagList(new ArrayList<>());
        }

        return aiModelVO;
    }

    @Override
    public List<AiModelVO> getAiModelVOList(List<AiModel> aiModelList) {
        if (aiModelList == null || aiModelList.isEmpty()) {
            return new ArrayList<>();
        }
        return aiModelList.stream().map(this::getAiModelVO).collect(Collectors.toList());
    }
}
