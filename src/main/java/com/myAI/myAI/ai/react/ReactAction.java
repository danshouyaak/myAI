package com.myAI.myAI.ai.react;

/**
 * ReAct行动类型枚举
 */
public enum ReactAction {
    SEARCH("搜索信息"),
    CALCULATE("计算"),
    ANALYZE("分析"),
    SUMMARIZE("总结"),
    QUERY_DATABASE("查询数据库"),
    CALL_API("调用API"),
    GENERATE_CODE("生成代码"),
    FINAL_ANSWER("给出最终答案");

    private final String description;

    ReactAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 