package com.myAI.myAI.constant;

/**
 * 操作类型枚举
 */
public enum OperationType {
    // 用户相关操作
    USER_LOGIN("用户登录"),
    USER_LOGOUT("用户注销"),
    USER_REGISTER("用户注册"),
    USER_UPDATE("用户信息更新"),

    // 会话相关操作
    CONVERSATION_CREATE("创建会话"),
    CONVERSATION_DELETE("删除会话"),
    CONVERSATION_LIST("获取会话列表"),
    
    // 消息相关操作
    MESSAGE_SEND("发送消息"),
    MESSAGE_RECEIVE("接收消息"),
    MESSAGE_LIST("获取消息列表"),
    
    // AI模型相关操作
    AI_MODEL_CALL("调用AI模型"),
    AI_MODEL_ERROR("AI模型错误"),
    
    // 文件操作
    FILE_UPLOAD("文件上传"),
    FILE_DOWNLOAD("文件下载");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 