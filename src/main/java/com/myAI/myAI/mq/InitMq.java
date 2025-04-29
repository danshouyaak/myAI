package com.myAI.myAI.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class InitMq {
    public static void main(String[] args) throws IOException, TimeoutException {
//        // 创建一个消息队列
        try {
            ConnectionFactory factory = new ConnectionFactory();
//            需要初始化的ip地址
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = UserMqConstant.USER_EXCHANGE_NAME;
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

            // 创建队列，随机分配一个队列名称
            String queueName = UserMqConstant.USER_QUEUE_NAME;
            channel.queueDeclare(queueName, true, false, false, null);
            // 绑定队列到交换机
            channel.queueBind(queueName, EXCHANGE_NAME, UserMqConstant.USER_ROUTING_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
