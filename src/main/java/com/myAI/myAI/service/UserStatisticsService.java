package com.myAI.myAI.service;

import com.myAI.myAI.models.vo.UserStatisticsVO;

/**
 * 用户统计服务接口
 */
public interface UserStatisticsService {

    /**
     * 获取用户统计数据
     */
    UserStatisticsVO getUserStatistics(Long userId);
}
