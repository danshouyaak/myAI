package com.myAI.myAI.mq;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.google.gson.Gson;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.models.entity.MessageFormat;
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

@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MessageService messageService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @RabbitListener(queues = {UserMqConstant.USER_QUEUE_NAME}, ackMode = "MANUAL")    // 指定监听哪个消息队列
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
//         消息为空，重新入队
        if (StringUtils.isAnyBlank(message)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        Gson gson = new Gson();
        Message messageObj = gson.fromJson(message, Message.class);

        log.info("receive message: {}", messageObj);

        boolean save = messageService.save(messageObj);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存消息失败");
        }
        MessageFormat messageFormat = new MessageFormat();
        BeanUtils.copyProperties(messageObj, messageFormat);

        Date sendTime = messageObj.getSendTime();
        String formatTime = DateUtil.format(sendTime, "yyyy-MM-dd HH:mm:ss");

        messageFormat.setSendTime(formatTime);

        redisTemplate.opsForList().rightPush(REDISKEY, gson.toJson(messageFormat));

        /*
        1913920064734629889
        1913925928728260610
         */

        // TODO 消息消费确认
        channel.basicAck(deliveryTag, false);
    }
}
