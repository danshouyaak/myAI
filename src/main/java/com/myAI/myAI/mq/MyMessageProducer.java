package com.myAI.myAI.mq;

import com.myAI.myAI.models.entity.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sedMessage(String message) {
        rabbitTemplate.convertAndSend(UserMqConstant.USER_EXCHANGE_NAME, UserMqConstant.USER_ROUTING_KEY, message);
    }
}
