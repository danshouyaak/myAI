package com.myAI.myAI.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.service.MessageService;
import com.myAI.myAI.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
 * @author yu
 * @description 针对表【message】的数据库操作Service实现
 * @createDate 2025-04-22 13:20:40
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

}




