package com.myAI.myAI.models.vo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

@Data
public class AIVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String aiId; // AI ID
    private String aiName; // AI名称
    private String modelVersion; // AI模型版本
    @TableLogic
    private Integer isDeleted; // 逻辑删除
}
