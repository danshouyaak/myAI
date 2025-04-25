package com.myAI.myAI.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public  class RedisInitUtil {
    @Autowired
    private RedisTemplate redisTemplate;

    public static RedisTemplate redisStatic;

    @PostConstruct
    public void getRedisTemplate(){
        redisStatic=this.redisTemplate;
    }
}



