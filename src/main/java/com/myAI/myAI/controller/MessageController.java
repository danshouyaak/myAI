package com.myAI.myAI.controller;

import com.google.gson.Gson;
import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.models.entity.MessageFormat;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.myAI.myAI.constant.RedisConstant.REDISKEY;

/**
 * 获取聊天信息
 */

@RestController
@RequestMapping("/message")
public class MessageController {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private UserService userService;

    @GetMapping("/getMessage/list")
    public BaseResponse<List<MessageFormat>> getMessageList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<String> result = redisTemplate.opsForList().range(REDISKEY, 0, -1);
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Gson gson = new Gson();
        List<MessageFormat> endResult = result.stream().map(s -> gson.fromJson(s, MessageFormat.class)).collect(Collectors.toList());
        return ResultUtils.success(endResult);
    }
}
