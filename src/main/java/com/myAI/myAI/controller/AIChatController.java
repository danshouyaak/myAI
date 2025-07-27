package com.myAI.myAI.controller;

import com.myAI.myAI.ai.react.ReactProcessor;
import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.AIRequestVO;
import com.myAI.myAI.service.AiService;
import com.myAI.myAI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * AI聊天接口
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class AIChatController {

    @Resource
    private AiService aiService;

    @Resource
    private UserService userService;

    @Resource
    private ReactProcessor reactProcessor;

    /**
     * AI对话（普通模式）
     */
    @PostMapping("/chat")
    public BaseResponse<String> doChat(@RequestBody AIRequestVO aiRequest, HttpServletRequest request) {
        // 1. 校验用户登录状态
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2. 校验参数
        String message = aiRequest.getMessage();
        if (StringUtils.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息不能为空");
        }

        // 3. 调用服务
        String result = aiService.doChat(message);
        return ResultUtils.success(result);
    }

    /**
     * AI对话（流式输出）
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter doChatStream(String message, HttpServletRequest request) {
        // 1. 校验用户登录状态
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2. 校验参数
        if (StringUtils.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息不能为空");
        }

        // 3. 调用服务
        return aiService.doChatStream(message);
    }

    /**
     * AI对话（ReAct思考模式）
     */
    @GetMapping(value = "/chat/react", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter doChatWithReact(String message, HttpServletRequest request) {
        // 1. 校验用户登录状态
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2. 校验参数
        if (StringUtils.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息不能为空");
        }

        // 3. 调用ReAct处理器
        log.info("开始ReAct思考模式处理，用户：{}，消息：{}", loginUser.getId(), message);
        return reactProcessor.processStreamSSE(message);
    }

    /**
     * AI对话（自动选择模式）
     * 
     * 根据用户输入自动选择是否使用ReAct模式：
     * 1. 如果问题需要搜索、计算、推理等复杂操作，使用ReAct模式
     * 2. 如果是简单对话，使用普通模式
     */
    @GetMapping(value = "/chat/auto", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter doChatAuto(String message, HttpServletRequest request) {
        // 1. 校验用户登录状态
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2. 校验参数
        if (StringUtils.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息不能为空");
        }

        // 3. 判断是否需要使用ReAct模式
        if (shouldUseReact(message)) {
            log.info("自动选择ReAct模式，用户：{}，消息：{}", loginUser.getId(), message);
            return reactProcessor.processStreamSSE(message);
        } else {
            log.info("自动选择普通模式，用户：{}，消息：{}", loginUser.getId(), message);
            return aiService.doChatStream(message);
        }
    }

    /**
     * 判断是否应该使用ReAct模式
     * 
     * 通过关键词和问题特征判断是否需要使用ReAct模式：
     * 1. 包含"搜索"、"查找"、"分析"等关键词
     * 2. 问题较长或包含多个子问题
     * 3. 需要最新信息或实时数据
     * 4. 需要计算或推理
     */
    private boolean shouldUseReact(String message) {
        // 关键词判断
        String[] reactKeywords = {
            "搜索", "查找", "查询", "分析", "比较",
            "最新", "最近", "实时", "现在",
            "计算", "统计", "推理", "预测",
            "如何", "为什么", "怎么样"
        };
        
        message = message.toLowerCase();
        for (String keyword : reactKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }

        // 问题复杂度判断
        if (message.length() > 50 || 
            message.contains("?") && message.split("\\?").length > 1 ||
            message.contains("？") && message.split("？").length > 1) {
            return true;
        }

        return false;
    }
}