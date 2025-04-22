package com.myAI.myAI.models.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.sql.Time;
import java.time.LocalDateTime;

@Data
public class Conversation {
    private String conversationId; // 对话ID
    private User user; // 用户对象
    private LocalDateTime startTime; // 对话开始时间
    private LocalDateTime endTime; // 对话结束时间
    private String conversationState; // 对话状态（活跃或已完成）
    @TableLogic
    private Integer isDeleted; // 逻辑删除

    // 结束对话的方法
    public void endConversation() {
        this.endTime = LocalDateTime.now();
        this.conversationState = "completed";
    }

    // 结束对话的方法
    public void endConversation(LocalDateTime time) {
        this.endTime = time;
        this.conversationState = "completed";
    }

}
