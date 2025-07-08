package com.myAI.myAI.controller;

import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.dto.UserLoginRequest;
import com.myAI.myAI.models.dto.UserRegisterRequest;
import com.myAI.myAI.models.dto.UserUpdateRequest;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.LoginUserVO;
import com.myAI.myAI.mq.MyMessageProducer;
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


    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
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
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
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
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        System.out.println(request);
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 用户更新
     *
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> userUpdate(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAvatar = userUpdateRequest.getUserAvatar();
        String userProfile = userUpdateRequest.getUserProfile();
        String userName = userUpdateRequest.getUserName();

        if (userName.length() > 16 || userName.length() < 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度不符合要求");
        }
        if (userProfile.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户简介长度不符合要求");
        }

        boolean result = userService.userUpdate(userName, userAvatar, userProfile, request);
        return ResultUtils.success(result);
    }
}
