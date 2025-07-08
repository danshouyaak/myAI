package com.myAI.myAI.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.LoginUserVO;
import com.myAI.myAI.models.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 用户更新信息
     *
     * @param request
     * @return
     */
    boolean userUpdate(String userName, String userAvatar,String userProfile,HttpServletRequest request);


    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);
}
