# . AI 表（AI）
CREATE TABLE AI
(
    aiId         VARCHAR(255) PRIMARY KEY,                                                    -- AI 唯一标识符
    aiName       VARCHAR(255) NOT NULL,                                                       -- AI 名称
    aiUrl        VARCHAR(255) NOT NULL,                                                       -- AI 头像地址
    modelVersion VARCHAR(255) NOT NULL,                                                       -- AI 模型版本
    description  TEXT,                                                                        -- AI 描述
    createdTime  DATETIME              DEFAULT CURRENT_TIMESTAMP,                             -- AI 创建时间
    updatedTime  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- AI 信息更新时间
    isDeleted    BOOLEAN      NOT NULL DEFAULT 0                                              -- 逻辑删除字段
);

# 对话表（Conversation）
CREATE TABLE conversation
(
    conversationId    VARCHAR(255) PRIMARY KEY,                                                                   -- 对话唯一标识符
    userId            VARCHAR(255)                 NOT NULL,                                                      -- 用户ID，与User表关联
    aiId              VARCHAR(255)                 NOT NULL,                                                      -- AI ID，与AI表关联
    startTime         DATETIME                     NOT NULL DEFAULT CURRENT_TIMESTAMP,                            -- 对话开始时间
    endTime           DATETIME,                                                                                   -- 对话结束时间（可选）
    conversationState ENUM ('active', 'completed') NOT NULL DEFAULT 'active',                                     -- 对话状态
    isDeleted         BOOLEAN                      NOT NULL DEFAULT 0,                                            -- 逻辑删除字段
    createdTime       DATETIME                              DEFAULT CURRENT_TIMESTAMP,                            -- 对话创建时间
    updatedTime       DATETIME                              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 对话信息更新时间
#                               FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, -- 外键约束，用户删除时对话也删除
#                               FOREIGN KEY (ai_id) REFERENCES ai(ai_id) ON DELETE CASCADE        -- 外键约束，AI删除时对话也删除
);

CREATE TABLE Message (
                         messageId BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, -- 消息唯一标识符（自增）
                         conversationId VARCHAR(255) NOT NULL,                 -- 对话ID，与Conversation表关联
                         messageContent TEXT NOT NULL,                         -- 消息内容
                         messageType ENUM('user', 'ai') NOT NULL,              -- 消息类型：用户或AI
                         sendTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 消息发送时间
                         isDeleted BOOLEAN NOT NULL DEFAULT 0                 -- 逻辑删除字段
#                          FOREIGN KEY (conversation_id) REFERENCES Conversation(conversation_id) ON DELETE CASCADE -- 外键约束，对话删除时消息也删除
);