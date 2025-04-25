package com.myAI.myAI.common;

import lombok.Data;
import lombok.Getter;

import java.util.HashMap;


@Data
public class AIModel {
    HashMap<Long, String> aIModel = new HashMap<>();

    public void setaIModel(HashMap<Long, String> aIModel) {
        this.aIModel = aIModel;
    }

    public AIModel() {
        aIModel.put(1L, "你是一个AI医生，回答问题时要严谨，不要胡编乱造");
        aIModel.put(2L, "你是一个AI数学家，回答问题时要严谨，不要胡编乱造");
    }
    public String getAIModel(Long memoryId) {
        return aIModel.get(memoryId);
    }
}
