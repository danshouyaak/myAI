package com.myAI.myAI.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myAI.myAI.models.entity.Conversation;
import com.myAI.myAI.models.vo.ConversationVO;
import com.myAI.myAI.service.ConversationService;
import com.myAI.myAI.mapper.ConversationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author yu
* @description 针对表【conversation】的数据库操作Service实现
* @createDate 2025-04-23 20:28:44
*/
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
    implements ConversationService{

    @Resource
    private ConversationMapper conversationMapper;

    @Override
    public ConversationVO getConversationWithAiInfo(String conversationId) {
        return conversationMapper.getConversationWithAiInfo(conversationId);
    }

    @Override
    public List<ConversationVO> getUserConversationsWithAiInfo(String userId) {
        return conversationMapper.getUserConversationsWithAiInfo(userId);
    }
}




