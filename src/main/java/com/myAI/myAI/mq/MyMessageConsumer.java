package com.myAI.myAI.mq;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.Ai;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.models.entity.MessageFormat;
import com.myAI.myAI.service.AiService;
import com.myAI.myAI.service.MessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.myAI.myAI.constant.RedisConstant.REDISKEY;
import static com.myAI.myAI.constant.RedisConstant.REDISKEYMESSAGE;

@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MessageService messageService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private AiService aiService;


    @RabbitListener(queues = {UserMqConstant.USER_QUEUE_NAME}, ackMode = "MANUAL")    // 指定监听哪个消息队列
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
//         消息为空，重新入队
        if (StringUtils.isAnyBlank(message)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        Gson gson = new Gson();
        Message messageObj = gson.fromJson(message, Message.class);

//        查找ai的信息
        Long aiId = messageObj.getAiId();
        if (aiId == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "aiId为空");
        }
        QueryWrapper<Ai> aiQueryWrapper = new QueryWrapper<>();
        aiQueryWrapper.eq("aiId", aiId);
        Ai ai = aiService.getOne(aiQueryWrapper);
        if (ai == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "ai模型不存在");
        }

        log.info("receive message: {}", messageObj);

//        设置ai的url和id到message中
        messageObj.setAiId(ai.getAiId());
        messageObj.setAiUrl(ai.getAiUrl());

        boolean save = messageService.save(messageObj);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存消息失败");
        }
        MessageFormat messageFormat = new MessageFormat();
        BeanUtils.copyProperties(messageObj, messageFormat);

        Date sendTime = messageObj.getSendTime();
        String formatTime = DateUtil.format(sendTime, "yyyy-MM-dd HH:mm:ss");

        messageFormat.setSendTime(formatTime);
        String key = REDISKEYMESSAGE + messageObj.getConversationId();

        redisTemplate.opsForList().rightPush(key, gson.toJson(messageFormat));

        /*
        1913920064734629889
        1913925928728260610
         */

        // TODO 消息消费确认
        channel.basicAck(deliveryTag, false);
    }
}
