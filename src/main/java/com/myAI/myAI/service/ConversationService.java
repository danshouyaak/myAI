package com.myAI.myAI.service;

import com.myAI.myAI.models.entity.Conversation;
import com.myAI.myAI.models.vo.ConversationVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yu
* @description 针对表【conversation】的数据库操作Service
* @createDate 2025-04-23 20:28:44
*/
public interface ConversationService extends IService<Conversation> {

    /**
     * 根据会话ID获取会话详情（包含AI模型信息）
     */
    ConversationVO getConversationWithAiInfo(String conversationId);

    /**
     * 获取用户的会话列表（包含AI模型信息）
     */
    List<ConversationVO> getUserConversationsWithAiInfo(String userId);
}
