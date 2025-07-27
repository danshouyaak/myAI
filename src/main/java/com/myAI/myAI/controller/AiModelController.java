package com.myAI.myAI.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.constant.UserConstant;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.dto.aimodel.AiModelAddRequest;
import com.myAI.myAI.models.dto.aimodel.AiModelQueryRequest;
import com.myAI.myAI.models.dto.aimodel.AiModelUpdateRequest;
import com.myAI.myAI.models.entity.AiModel;
import com.myAI.myAI.models.entity.AiModelCategory;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.AiModelVO;
import com.myAI.myAI.service.AiModelService;
import com.myAI.myAI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * AI模型控制器
 */
@RestController
@RequestMapping("/aimodel")
@Slf4j
public class AiModelController {

    @Resource
    private AiModelService aiModelService;

    @Resource
    private UserService userService;

    /**
     * 添加AI模型
     */
    @PostMapping("/add")
    public BaseResponse<Long> addAiModel(@Valid @RequestBody AiModelAddRequest addRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        // 检查权限：只有管理员可以添加模型
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        Long aiModelId = aiModelService.addAiModel(addRequest, loginUser.getId());
        return ResultUtils.success(aiModelId);
    }

    /**
     * 更新AI模型
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateAiModel(@Valid @RequestBody AiModelUpdateRequest updateRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        // 检查权限：只有管理员可以更新模型
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = aiModelService.updateAiModel(updateRequest, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 删除AI模型
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAiModel(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        // 检查权限：只有管理员可以删除模型
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = aiModelService.deleteAiModel(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取AI模型
     */
    @GetMapping("/get")
    public BaseResponse<AiModelVO> getAiModelById(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiModelVO aiModelVO = aiModelService.getAiModelVOById(id);
        return ResultUtils.success(aiModelVO);
    }

    /**
     * 根据ID获取AI模型详情（包含prompt）
     */
    @GetMapping("/detail")
    public BaseResponse<AiModel> getAiModelDetail(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 验证用户登录
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        AiModel aiModel = aiModelService.getAiModelById(id);
        if (aiModel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        
        // 增加使用次数
        aiModelService.incrementUsageCount(id);
        
        return ResultUtils.success(aiModel);
    }

    /**
     * 分页查询AI模型
     */
    @PostMapping("/list/page")
    public BaseResponse<IPage<AiModelVO>> listAiModelByPage(@RequestBody AiModelQueryRequest queryRequest) {
        IPage<AiModelVO> aiModelVOPage = aiModelService.listAiModelVOByPage(queryRequest);
        return ResultUtils.success(aiModelVOPage);
    }

    /**
     * 根据分类获取AI模型列表
     */
    @GetMapping("/list/category")
    public BaseResponse<List<AiModelVO>> listAiModelByCategory(@RequestParam String category) {
        List<AiModelVO> aiModelVOList = aiModelService.listAiModelByCategory(category);
        return ResultUtils.success(aiModelVOList);
    }

    /**
     * 获取推荐AI模型列表
     */
    @GetMapping("/list/recommend")
    public BaseResponse<List<AiModelVO>> listRecommendAiModels() {
        List<AiModelVO> aiModelVOList = aiModelService.listRecommendAiModels();
        return ResultUtils.success(aiModelVOList);
    }

    /**
     * 获取热门AI模型列表
     */
    @GetMapping("/list/popular")
    public BaseResponse<List<AiModelVO>> listPopularAiModels(@RequestParam(defaultValue = "10") int limit) {
        List<AiModelVO> aiModelVOList = aiModelService.listPopularAiModels(limit);
        return ResultUtils.success(aiModelVOList);
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/category/list")
    public BaseResponse<List<AiModelCategory>> listAllCategories() {
        List<AiModelCategory> categoryList = aiModelService.listActiveCategories();
        return ResultUtils.success(categoryList);
    }

    /**
     * 搜索AI模型
     */
    @GetMapping("/search")
    public BaseResponse<List<AiModelVO>> searchAiModels(@RequestParam String keyword) {
        AiModelQueryRequest queryRequest = new AiModelQueryRequest();
        queryRequest.setKeyword(keyword);
        queryRequest.setIsActive(1);
        queryRequest.setPageSize(20);
        
        IPage<AiModelVO> aiModelVOPage = aiModelService.listAiModelVOByPage(queryRequest);
        return ResultUtils.success(aiModelVOPage.getRecords());
    }
}
