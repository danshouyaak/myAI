package com.myAI.myAI.controller;

import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.constant.OperationType;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.dto.UserLoginRequest;
import com.myAI.myAI.models.dto.UserRegisterRequest;
import com.myAI.myAI.models.dto.UserUpdateRequest;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.LoginUserVO;
import com.myAI.myAI.mq.MyMessageProducer;
import com.myAI.myAI.service.OperationLogService;
import com.myAI.myAI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        long userId;
        try {
            userId = userService.userRegister(userAccount, userPassword, checkPassword);
            // 注册成功，记录操作日志
            operationLogService.asyncRecordOperationLog(
                userId,
                OperationType.USER_REGISTER,
                String.format("用户 %s 注册成功", userAccount),
                true,
                String.valueOf(userId),
                null
            );
        } catch (BusinessException e) {
            // 注册失败，记录操作日志
            operationLogService.asyncRecordOperationLog(
                null,
                OperationType.USER_REGISTER,
                String.format("用户 %s 注册失败：%s", userAccount, e.getMessage()),
                false,
                null,
                null
            );
            throw e;
        }
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO;
        try {
            loginUserVO = userService.userLogin(userAccount, userPassword, request);
            // 登录成功，记录操作日志
            operationLogService.asyncRecordOperationLog(
                loginUserVO.getId(),
                OperationType.USER_LOGIN,
                String.format("用户 %s 登录成功", userAccount),
                true,
                String.valueOf(loginUserVO.getId()),
                request
            );
        } catch (BusinessException e) {
            // 登录失败，记录操作日志
            operationLogService.asyncRecordOperationLog(
                null,
                OperationType.USER_LOGIN,
                String.format("用户 %s 登录失败：%s", userAccount, e.getMessage()),
                false,
                null,
                request
            );
            throw e;
        }
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result;
        try {
            result = userService.userLogout(request);
            // 注销成功，记录操作日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.USER_LOGOUT,
                String.format("用户 %s 注销成功", loginUser.getUserAccount()),
                true,
                String.valueOf(loginUser.getId()),
                request
            );
        } catch (BusinessException e) {
            // 注销失败，记录操作日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.USER_LOGOUT,
                String.format("用户 %s 注销失败：%s", loginUser.getUserAccount(), e.getMessage()),
                false,
                String.valueOf(loginUser.getId()),
                request
            );
            throw e;
        }
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 用户更新个人信息
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> userUpdate(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String userAvatar = userUpdateRequest.getUserAvatar();
        String userProfile = userUpdateRequest.getUserProfile();
        String userName = userUpdateRequest.getUserName();

        if (userName.length() > 16 || userName.length() < 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度不符合要求");
        }
        if (userProfile.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户简介长度不符合要求");
        }

        boolean result;
        try {
            result = userService.userUpdate(userName, userAvatar, userProfile, request);
            // 更新成功，记录操作日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.USER_UPDATE,
                String.format("用户 %s 更新个人信息成功", loginUser.getUserAccount()),
                true,
                String.valueOf(loginUser.getId()),
                request
            );
        } catch (BusinessException e) {
            // 更新失败，记录操作日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.USER_UPDATE,
                String.format("用户 %s 更新个人信息失败：%s", loginUser.getUserAccount(), e.getMessage()),
                false,
                String.valueOf(loginUser.getId()),
                request
            );
            throw e;
        }
        return ResultUtils.success(result);
    }
}
