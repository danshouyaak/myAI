package com.myAI.myAI.mapper;

import com.myAI.myAI.models.entity.Conversation;
import com.myAI.myAI.models.vo.ConversationVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author yu
* @description 针对表【conversation】的数据库操作Mapper
* @createDate 2025-04-23 20:28:44
* @Entity com.myAI.myAI.models.entity.Conversation
*/
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 根据会话ID获取会话详情（包含AI模型信息）
     */
    @Select("SELECT c.conversationId, c.userId, c.aiId, c.description, c.startTime, c.endTime, " +
            "c.conversationState, c.createdTime, c.updatedTime, " +
            "COALESCE(am.name, ai.aiName, 'MyAI助手') as aiName, " +
            "COALESCE(am.icon, 'MyAIlogin.svg') as aiIcon " +
            "FROM conversation c " +
            "LEFT JOIN aimodel am ON CAST(c.aiId AS UNSIGNED) = am.id " +
            "LEFT JOIN ai ai ON CAST(c.aiId AS UNSIGNED) = ai.aiId " +
            "WHERE c.conversationId = #{conversationId} AND c.isDeleted = 0")
    ConversationVO getConversationWithAiInfo(@Param("conversationId") String conversationId);

    /**
     * 获取用户的会话列表（包含AI模型信息）
     */
    @Select("SELECT c.conversationId, c.userId, c.aiId, c.description, c.startTime, c.endTime, " +
            "c.conversationState, c.createdTime, c.updatedTime, " +
            "COALESCE(am.name, ai.aiName, 'MyAI助手') as aiName, " +
            "COALESCE(am.icon, 'MyAIlogin.svg') as aiIcon " +
            "FROM conversation c " +
            "LEFT JOIN aimodel am ON CAST(c.aiId AS UNSIGNED) = am.id " +
            "LEFT JOIN ai ai ON CAST(c.aiId AS UNSIGNED) = ai.aiId " +
            "WHERE c.userId = #{userId} AND c.isDeleted = 0 " +
            "ORDER BY c.updatedTime DESC")
    List<ConversationVO> getUserConversationsWithAiInfo(@Param("userId") String userId);
}




