package com.myAI.myAI.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.dto.ConversationRequest;
import com.myAI.myAI.models.entity.Conversation;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.service.ConversationService;
import com.myAI.myAI.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.myAI.myAI.constant.RedisConstant.REDISKEYCONVERSATION;

@RestController
@RequestMapping("/conversation")
public class ConversationController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ConversationService conversationService;
    @Autowired
    private Gson gson;


    @GetMapping("/getConversation/list")
    public BaseResponse<List<Conversation>> getConversationList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long userId = loginUser.getId();
        String key = REDISKEYCONVERSATION + userId;
        List<String> redisResult = redisTemplate.opsForList().range(key, 0, -1);
//        如果缓存不为空，直接返回缓存数据
        if (!CollUtil.isEmpty(redisResult)) {
            Gson gson = new Gson();
            List<Conversation> collect = redisResult.stream().map(s -> gson.fromJson(s, Conversation.class)).collect(Collectors.toList());
            return ResultUtils.success(collect);
        }

//        如果缓存为空 去数据库查询
        QueryWrapper<Conversation> conversationQueryWrapper = new QueryWrapper<>();
        conversationQueryWrapper.eq("userId", userId);
        List<Conversation> mysqlResult = conversationService.list(conversationQueryWrapper);

        List<String> collect = mysqlResult.stream().map(s -> gson.toJson(s)).collect(Collectors.toList());

//        存储到redis
        redisTemplate.opsForList().leftPushAll(key, collect);


        return ResultUtils.success(mysqlResult);
    }

    @PostMapping("/addConversation")
    public BaseResponse<Boolean> addConversation(@RequestBody ConversationRequest conversationRequest,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Conversation conversation = new Conversation();
        BeanUtils.copyProperties(conversationRequest, conversation);
        conversation.setUserId(String.valueOf(loginUser.getId()));
        conversation.setConversationState("active");
        boolean save = conversationService.save(conversation);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        String key = REDISKEYCONVERSATION + loginUser.getId();
        redisTemplate.opsForList().leftPush(key, gson.toJson(conversation));
        return ResultUtils.success(save);
    }
}
