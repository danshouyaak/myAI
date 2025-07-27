package com.myAI.myAI.service.Impl;

import com.myAI.myAI.models.vo.UserStatisticsVO;
import com.myAI.myAI.mapper.UserStatisticsMapper;
import com.myAI.myAI.service.UserStatisticsService;
import com.myAI.myAI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户统计服务实现
 */
@Slf4j
@Service
public class UserStatisticsServiceImpl implements UserStatisticsService {

    @Resource
    private UserStatisticsMapper userStatisticsMapper;

    @Resource
    private UserService userService;

    @Override
    public UserStatisticsVO getUserStatistics(Long userId) {
        log.info("📊 获取用户统计数据，用户ID: {}", userId);

        UserStatisticsVO statisticsVO = new UserStatisticsVO();

        try {
            // 1. 获取基本统计信息
            UserStatisticsVO.UserBasicStats basicStats = userStatisticsMapper.getUserBasicStats(userId);
            if (basicStats == null) {
                // 如果没有真实数据，创建空的统计数据
                basicStats = new UserStatisticsVO.UserBasicStats();
                basicStats.setTotalConversations(0L);
                basicStats.setTotalMessages(0L);
                basicStats.setTotalTokensUsed(0L);
                basicStats.setTotalThinkingSteps(0L);
                basicStats.setActiveDays(0);
                log.info("📊 用户暂无统计数据，返回空数据");
            }

            // 获取最常用的AI模型
            String mostUsedModel = userStatisticsMapper.getMostUsedAiModel(userId);
            basicStats.setMostUsedAiModelName(mostUsedModel != null ? mostUsedModel : "暂无");

            // 获取用户注册时间
            var user = userService.getById(userId);
            if (user != null && user.getCreateTime() != null) {
                basicStats.setJoinDate(user.getCreateTime().toString());
            }

            statisticsVO.setBasicStats(basicStats);

            // 2. 获取AI模型使用统计
            List<UserStatisticsVO.AiModelUsageStats> aiModelUsage = userStatisticsMapper.getUserAiModelUsage(userId);

            // 如果没有真实数据，使用空列表
            if (aiModelUsage == null) {
                aiModelUsage = new ArrayList<>();
                log.info("📊 用户暂无AI模型使用数据");
            }

            // 计算使用百分比
            if (aiModelUsage != null && !aiModelUsage.isEmpty()) {
                int totalUsage = aiModelUsage.stream().mapToInt(UserStatisticsVO.AiModelUsageStats::getUsageCount).sum();
                aiModelUsage.forEach(usage -> {
                    if (totalUsage > 0) {
                        double percentage = (double) usage.getUsageCount() / totalUsage * 100;
                        usage.setUsagePercentage(Math.round(percentage * 100.0) / 100.0);
                    } else {
                        usage.setUsagePercentage(0.0);
                    }
                });
            }

            statisticsVO.setAiModelUsage(aiModelUsage);

            // 3. 获取活跃度趋势
            List<UserStatisticsVO.ActivityTrendStats> activityTrend = userStatisticsMapper.getUserActivityTrend(userId);
            if (activityTrend == null) {
                activityTrend = new ArrayList<>();
                log.info("📊 用户暂无活跃度趋势数据");
            } else {
                // 格式化日期
                activityTrend.forEach(trend -> {
                    if (trend.getDate() != null) {
                        // 确保日期格式正确
                        trend.setDate(trend.getDate());
                    }
                });
            }
            statisticsVO.setActivityTrend(activityTrend);

            // 4. 获取时段分布
            List<Map<String, Object>> hourlyData = userStatisticsMapper.getUserHourlyDistribution(userId);
            Map<Integer, Integer> hourlyDistribution;

            // 填充实际数据
            hourlyDistribution = new HashMap<>();
            // 初始化24小时数据
            for (int i = 0; i < 24; i++) {
                hourlyDistribution.put(i, 0);
            }

            if (hourlyData != null && !hourlyData.isEmpty()) {
                // 使用final变量在lambda中引用
                final Map<Integer, Integer> finalHourlyDistribution = hourlyDistribution;
                hourlyData.forEach(data -> {
                    Integer hour = (Integer) data.get("hour");
                    Long count = (Long) data.get("messageCount");
                    if (hour != null && count != null) {
                        finalHourlyDistribution.put(hour, count.intValue());
                    }
                });
                log.info("📊 获取到{}条时段分布数据", hourlyData.size());
            } else {
                log.info("📊 用户暂无时段分布数据");
            }

            statisticsVO.setHourlyDistribution(hourlyDistribution);

            log.info("✅ 用户统计数据获取成功，用户ID: {}", userId);
            return statisticsVO;

        } catch (Exception e) {
            log.error("❌ 获取用户统计数据失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            // 返回空的统计数据
            return createEmptyStatistics();
        }
    }





    /**
     * 创建空的统计数据
     */
    private UserStatisticsVO createEmptyStatistics() {
        UserStatisticsVO statisticsVO = new UserStatisticsVO();

        UserStatisticsVO.UserBasicStats basicStats = new UserStatisticsVO.UserBasicStats();
        basicStats.setTotalConversations(0L);
        basicStats.setTotalMessages(0L);
        basicStats.setTotalTokensUsed(0L);
        basicStats.setTotalThinkingSteps(0L);
        basicStats.setActiveDays(0);
        basicStats.setMostUsedAiModelName("暂无");

        statisticsVO.setBasicStats(basicStats);
        statisticsVO.setAiModelUsage(List.of());
        statisticsVO.setActivityTrend(List.of());
        statisticsVO.setHourlyDistribution(new HashMap<>());

        return statisticsVO;
    }
}
