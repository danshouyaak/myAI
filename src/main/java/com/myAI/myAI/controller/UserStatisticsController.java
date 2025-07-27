package com.myAI.myAI.controller;

import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.UserStatisticsVO;
import com.myAI.myAI.service.UserService;
import com.myAI.myAI.service.UserStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/statistics")
public class UserStatisticsController {

    @Resource
    private UserStatisticsService userStatisticsService;

    @Resource
    private UserService userService;

    /**
     * 获取当前用户的统计数据
     */
    @GetMapping("/my")
    public BaseResponse<UserStatisticsVO> getMyStatistics(HttpServletRequest request) {
        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);
            if (loginUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }

            log.info("📊 获取用户统计数据请求，用户ID: {}", loginUser.getId());

            // 获取统计数据
            UserStatisticsVO statistics = userStatisticsService.getUserStatistics(loginUser.getId());

            log.info("✅ 用户统计数据获取成功，用户ID: {}", loginUser.getId());
            return ResultUtils.success(statistics);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 获取用户统计数据失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取统计数据失败");
        }
    }


}
