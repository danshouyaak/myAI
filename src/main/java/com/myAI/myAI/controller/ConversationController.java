package com.myAI.myAI.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.gson.Gson;
import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.constant.OperationType;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.dto.ConversationRequest;
import com.myAI.myAI.models.entity.Conversation;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.ConversationVO;
import com.myAI.myAI.service.ConversationService;
import com.myAI.myAI.service.OperationLogService;
import com.myAI.myAI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.myAI.myAI.constant.RedisConstant.REDISKEYCONVERSATION;

@Slf4j
@RestController
@RequestMapping("/conversation")
public class ConversationController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ConversationService conversationService;

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private Gson gson;

    @GetMapping("/getConversation/list")
    public BaseResponse<List<Conversation>> getConversationList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long userId = loginUser.getId();
        List<Conversation> result = new ArrayList<>();
        try {
            String key = REDISKEYCONVERSATION + userId;
            List<String> redisResult = redisTemplate.opsForList().range(key, 0, -1);
            // 如果缓存不为空，直接返回缓存数据
            if (!CollUtil.isEmpty(redisResult)) {
                result = redisResult.stream()
                    .map(s -> gson.fromJson(s, Conversation.class))
                    .collect(Collectors.toList());
            } else {
                // 如果缓存为空 去数据库查询
                QueryWrapper<Conversation> conversationQueryWrapper = new QueryWrapper<>();
                conversationQueryWrapper.eq("userId", userId);
                result = conversationService.list(conversationQueryWrapper);
                if (!CollUtil.isEmpty(result)) {
                    List<String> collect = result.stream()
                        .map(s -> gson.toJson(s))
                        .collect(Collectors.toList());
                    // 存储到redis
                    redisTemplate.opsForList().leftPushAll(key, collect);
                }
            }
            // 记录操作日志
            operationLogService.asyncRecordOperationLog(
                userId,
                OperationType.CONVERSATION_LIST,
                "获取会话列表",
                true,
                String.valueOf(userId),
                request
            );
        } catch (Exception e) {
            // 记录操作失败日志
            operationLogService.asyncRecordOperationLog(
                userId,
                OperationType.CONVERSATION_LIST,
                "获取会话列表失败：" + e.getMessage(),
                false,
                String.valueOf(userId),
                request
            );
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取会话列表失败");
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/addConversation")
    public BaseResponse<String> addConversation(@RequestBody ConversationRequest conversationRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        String conversationId;
        try {
            Conversation conversation = new Conversation();
            BeanUtils.copyProperties(conversationRequest, conversation);
            conversation.setUserId(String.valueOf(loginUser.getId()));
            conversation.setConversationState("active");

            // 确保aiId正确设置
            if (conversationRequest.getAiId() != null) {
                conversation.setAiId(conversationRequest.getAiId());
                log.info("🤖 创建会话，AI模型ID: {}", conversationRequest.getAiId());
            }
            boolean save = conversationService.save(conversation);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }

            String key = REDISKEYCONVERSATION + loginUser.getId();
            redisTemplate.opsForList().leftPush(key, gson.toJson(conversation));
            conversationId = conversation.getConversationId();

            // 记录操作日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.CONVERSATION_CREATE,
                "创建新会话",
                true,
                conversationId,
                request
            );


        } catch (Exception e) {
            // 记录操作失败日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.CONVERSATION_CREATE,
                "创建会话失败：" + e.getMessage(),
                false,
                null,
                request
            );
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建会话失败");
        }
        return ResultUtils.success(conversationId);
    }

    @GetMapping("/deleteConversation")
    public BaseResponse<Boolean> deleteConversation(String conversationId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (StrUtil.isEmpty(conversationId) || StrUtil.isBlank(conversationId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = loginUser.getId();
        try {
            UpdateWrapper<Conversation> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("userId", userId);
            updateWrapper.eq("conversationId", conversationId);
            boolean remove = conversationService.remove(updateWrapper);
            redisTemplate.delete(REDISKEYCONVERSATION + userId);

            // 记录操作日志
            operationLogService.asyncRecordOperationLog(
                userId,
                OperationType.CONVERSATION_DELETE,
                "删除会话",
                remove, // 使用实际的删除结果
                conversationId,
                request
            );

            return ResultUtils.success(remove);
        } catch (Exception e) {
            // 记录操作失败日志
            operationLogService.asyncRecordOperationLog(
                userId,
                OperationType.CONVERSATION_DELETE,
                "删除会话失败：" + e.getMessage(),
                false,
                conversationId,
                request
            );
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除会话失败");
        }
    }

    /**
     * 获取会话详情（包含AI模型信息）
     */
    @GetMapping("/detail/{conversationId}")
    public BaseResponse<ConversationVO> getConversationDetail(@PathVariable String conversationId, HttpServletRequest request) {
        if (StrUtil.isBlank(conversationId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空");
        }

        try {
            log.info("🔍 获取会话详情，会话ID: {}", conversationId);

            // 获取当前用户
            User loginUser = userService.getLoginUser(request);
            log.info("👤 当前用户ID: {}", loginUser.getId());

            // 获取会话详情
            ConversationVO conversationVO = conversationService.getConversationWithAiInfo(conversationId);
            log.info("📋 查询到的会话信息: {}", conversationVO);

            if (conversationVO == null) {
                log.warn("⚠️ 会话不存在: {}", conversationId);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
            }

            // 验证会话所有权
            if (!conversationVO.getUserId().equals(String.valueOf(loginUser.getId()))) {
                log.warn("⚠️ 无权访问会话，会话用户ID: {}, 当前用户ID: {}", conversationVO.getUserId(), loginUser.getId());
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问此会话");
            }

            log.info("✅ 成功获取会话详情，AI名称: {}, AI头像: {}", conversationVO.getAiName(), conversationVO.getAiIcon());
            return ResultUtils.success(conversationVO);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取会话详情失败");
        }
    }

    /**
     * 获取用户会话列表（包含AI模型信息）
     */
    @GetMapping("/list/withAiInfo")
    public BaseResponse<List<ConversationVO>> getUserConversationsWithAiInfo(HttpServletRequest request) {
        try {
            // 获取当前用户
            User loginUser = userService.getLoginUser(request);

            // 获取用户会话列表
            List<ConversationVO> conversationVOList = conversationService.getUserConversationsWithAiInfo(String.valueOf(loginUser.getId()));

            return ResultUtils.success(conversationVOList);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取会话列表失败");
        }
    }
}
