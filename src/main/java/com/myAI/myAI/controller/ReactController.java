package com.myAI.myAI.controller;

import com.myAI.myAI.ai.react.ReactMemoryManager;
import com.myAI.myAI.ai.react.ReactProcessor;
import com.myAI.myAI.ai.react.ReactThought;
import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/react")
@Slf4j
public class ReactController {

    @Resource
    private ReactProcessor reactProcessor;

    @Resource
    private ReactMemoryManager memoryManager;

    @Resource
    private UserService userService;

    /**
     * 流式处理用户输入（SSE）- 使用 POST 请求
     */
    @PostMapping(value = "/process/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter processStream(@RequestBody Map<String, String> requestBody,
                                   HttpServletRequest request) {
        String input = requestBody.get("input");
        String conversationId = requestBody.get("conversationId");

        if (input == null || input.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入内容不能为空");
        }

        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空");
        }

        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long sessionId = loginUser.getId();
        log.info("收到流式处理请求，输入：{}, 会话ID：{}, 用户ID：{}", input, conversationId, sessionId);
        return reactProcessor.processStreamSSE(input, conversationId, sessionId);
    }

    /**
     * 处理用户输入（普通）
     */
    @GetMapping("/process")
    public BaseResponse<ReactThought> process(@RequestParam String input) {
        log.info("收到处理请求，输入：{}", input);
        ReactThought thought = new ReactThought();
        thought.setThought("该接口已弃用，请使用 /react/process/stream 流式接口");
        thought.setDone(true);
        return ResultUtils.success(thought);
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/memory/history")
    public BaseResponse<List<String>> getSessionHistory(HttpServletRequest  request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long sessionId = loginUser.getId();
        log.info("获取会话历史，会话ID：{}", sessionId);
        List<String> history = memoryManager.getSessionHistory(String.valueOf(sessionId));
        return ResultUtils.success(history);
    }

    /**
     * 获取会话统计信息
     */
    @GetMapping("/memory/stats")
    public BaseResponse<Map<String, Object>> getSessionStats(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long sessionId = loginUser.getId();
        log.info("获取会话统计信息，会话ID：{}", sessionId);
        Map<String, Object> stats = memoryManager.getStatistics(String.valueOf(sessionId));
        return ResultUtils.success(stats);
    }

    /**
     * 清理会话数据
     */
    @DeleteMapping("/memory/clear")
    public BaseResponse<String> clearSession(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long sessionId = loginUser.getId();
        log.info("清理会话数据，会话ID：{}", sessionId);
        memoryManager.clearSession(String.valueOf(sessionId));
        return ResultUtils.success("会话数据已清理");
    }
} 