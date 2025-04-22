package com.myAI.myAI.mq;

import com.google.gson.Gson;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.service.MessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MessageService messageService;

    @RabbitListener(queues = {UserMqConstant.USER_QUEUE_NAME}, ackMode = "MANUAL")    // 指定监听哪个消息队列
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
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


        /*
        1913920064734629889
        1913925928728260610
         */

        // TODO 消息消费确认
        channel.basicAck(deliveryTag, false);
    }
}
