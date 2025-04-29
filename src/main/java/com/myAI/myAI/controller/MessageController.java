package com.myAI.myAI.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.models.entity.MessageFormat;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.service.MessageService;
import com.myAI.myAI.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.myAI.myAI.constant.RedisConstant.REDISKEY;
import static com.myAI.myAI.constant.RedisConstant.REDISKEYMESSAGE;

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

    @Resource
    private MessageService messageService;

    @GetMapping("/getMessage/list")
    public BaseResponse<List<MessageFormat>> getMessageList(String conversationId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (conversationId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<String> result = redisTemplate.opsForList().range(REDISKEY, 0, -1);
        if (result != null) {
            Gson gson = new Gson();
            List<MessageFormat> endResult = result.stream().map(s -> gson.fromJson(s, MessageFormat.class)).collect(Collectors.toList());
            ResultUtils.success(endResult);
        }

        String key = REDISKEYMESSAGE + conversationId;
//        查找数据库
        QueryWrapper<Message> messageQueryWrapper = new QueryWrapper<>();
        messageQueryWrapper.eq("conversationId", conversationId);
        List<Message> mysqlResult = messageService.list(messageQueryWrapper);
        if (mysqlResult == null) {
            return ResultUtils.success(new ArrayList<>());
        }

        Gson gson = new Gson();

        List<String> redisResult = new ArrayList<>();


        List<MessageFormat> collect = mysqlResult.stream().map(message -> {
            MessageFormat messageFormat = new MessageFormat();
            BeanUtils.copyProperties(message, messageFormat);
            Date sendTime = message.getSendTime();
            String formatTime = DateUtil.format(sendTime, "yyyy-MM-dd HH:mm:ss");
            messageFormat.setSendTime(formatTime);
            redisResult.add(gson.toJson(messageFormat));
            return messageFormat;
        }).collect(Collectors.toList());

        if (!CollUtil.isEmpty(collect)) {
            redisTemplate.delete(key);
//        插入到redis中
            redisTemplate.opsForList().rightPushAll(key, redisResult);
        }
        return ResultUtils.success(collect);

    }
}
