package com.myAI.myAI.common;

import lombok.Getter;

import java.util.HashMap;


@Getter
public class AIModel {
    HashMap<Long, String> aIModel = new HashMap<>();

    public AIModel() {
        aIModel.put(1L, "你是一个AI医生，回答问题时要严谨，不要胡编乱造");
        aIModel.put(2L, "你是一个AI数学家，回答问题时要严谨，不要胡编乱造");
    }
}
