package com.myAI.myAI.mapper;

import com.myAI.myAI.models.vo.UserStatisticsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户统计Mapper
 */
public interface UserStatisticsMapper {

    /**
     * 获取用户基本统计信息
     */
    @Select("SELECT " +
            "COUNT(DISTINCT c.conversationId) as totalConversations, " +
            "COUNT(m.messageId) as totalMessages, " +
            "COALESCE(SUM(CHAR_LENGTH(m.messageContent)), 0) as totalTokensUsed, " +
            "COUNT(CASE WHEN m.thinkingProcess IS NOT NULL AND m.thinkingProcess != '' THEN 1 END) as totalThinkingSteps, " +
            "0 as averageResponseTime, " +
            "COUNT(DISTINCT DATE(c.createdTime)) as activeDays " +
            "FROM conversation c " +
            "LEFT JOIN message m ON c.conversationId = m.conversationId " +
            "WHERE c.userId = #{userId} AND c.isDeleted = 0")
    UserStatisticsVO.UserBasicStats getUserBasicStats(@Param("userId") Long userId);

    /**
     * 获取用户AI模型使用统计
     */
    @Select("SELECT " +
            "c.aiId as aiModelId, " +
            "COALESCE(am.name, 'MyAI助手') as aiModelName, " +
            "COALESCE(am.icon, 'MyAIlogin.svg') as aiModelIcon, " +
            "COUNT(DISTINCT c.conversationId) as usageCount, " +
            "COUNT(m.messageId) as totalMessages, " +
            "COALESCE(SUM(CHAR_LENGTH(m.messageContent)), 0) as totalTokens, " +
            "0 as averageResponseTime " +
            "FROM conversation c " +
            "LEFT JOIN message m ON c.conversationId = m.conversationId " +
            "LEFT JOIN aimodel am ON CAST(c.aiId AS UNSIGNED) = am.id " +
            "WHERE c.userId = #{userId} AND c.isDeleted = 0 " +
            "GROUP BY c.aiId, am.name, am.icon " +
            "HAVING COUNT(DISTINCT c.conversationId) > 0 " +
            "ORDER BY usageCount DESC")
    List<UserStatisticsVO.AiModelUsageStats> getUserAiModelUsage(@Param("userId") Long userId);

    /**
     * 获取用户活跃度趋势（最近30天）
     */
    @Select("SELECT " +
            "DATE(c.createdTime) as date, " +
            "COUNT(m.messageId) as messageCount, " +
            "COUNT(DISTINCT c.conversationId) as conversationCount, " +
            "0 as sessionDuration, " +
            "0 as loginCount " +
            "FROM conversation c " +
            "LEFT JOIN message m ON c.conversationId = m.conversationId " +
            "WHERE c.userId = #{userId} AND c.isDeleted = 0 " +
            "AND c.createdTime >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(c.createdTime) " +
            "ORDER BY date DESC")
    List<UserStatisticsVO.ActivityTrendStats> getUserActivityTrend(@Param("userId") Long userId);

    /**
     * 获取用户时段分布
     */
    @Select("SELECT " +
            "HOUR(m.sendTime) as hour, " +
            "COUNT(m.messageId) as messageCount " +
            "FROM message m " +
            "JOIN conversation c ON m.conversationId = c.conversationId " +
            "WHERE c.userId = #{userId} AND c.isDeleted = 0 " +
            "GROUP BY HOUR(m.sendTime) " +
            "ORDER BY hour")
    List<Map<String, Object>> getUserHourlyDistribution(@Param("userId") Long userId);

    /**
     * 获取用户最常用的AI模型
     */
    @Select("SELECT " +
            "COALESCE(am.name, 'MyAI助手') as modelName " +
            "FROM conversation c " +
            "LEFT JOIN aimodel am ON CAST(c.aiId AS UNSIGNED) = am.id " +
            "WHERE c.userId = #{userId} AND c.isDeleted = 0 " +
            "GROUP BY c.aiId, am.name " +
            "ORDER BY COUNT(c.conversationId) DESC " +
            "LIMIT 1")
    String getMostUsedAiModel(@Param("userId") Long userId);
}
