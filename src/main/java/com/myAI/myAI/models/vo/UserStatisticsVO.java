package com.myAI.myAI.models.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户统计数据视图对象
 */
@Data
public class UserStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户基本统计
     */
    private UserBasicStats basicStats;

    /**
     * AI模型使用统计
     */
    private List<AiModelUsageStats> aiModelUsage;

    /**
     * 活跃度趋势
     */
    private List<ActivityTrendStats> activityTrend;

    /**
     * 时段分布
     */
    private Map<Integer, Integer> hourlyDistribution;

    /**
     * 用户基本统计
     */
    @Data
    public static class UserBasicStats {
        private Long totalConversations;
        private Long totalMessages;
        private Long totalTokensUsed;
        private Long totalThinkingSteps;
        private BigDecimal averageResponseTime;
        private Integer activeDays;
        private String mostUsedAiModelName;
        private String joinDate;
    }

    /**
     * AI模型使用统计
     */
    @Data
    public static class AiModelUsageStats {
        private Long aiModelId;
        private String aiModelName;
        private String aiModelIcon;
        private Integer usageCount;
        private Integer totalMessages;
        private Long totalTokens;
        private BigDecimal averageResponseTime;
        private Double usagePercentage;
    }

    /**
     * 活跃度趋势统计
     */
    @Data
    public static class ActivityTrendStats {
        private String date;
        private Integer messageCount;
        private Integer conversationCount;
        private Long sessionDuration;
        private Integer loginCount;
    }
}
