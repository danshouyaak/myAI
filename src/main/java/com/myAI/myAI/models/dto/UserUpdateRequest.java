package com.myAI.myAI.models.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;
}
