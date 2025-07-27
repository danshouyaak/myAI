package com.myAI.myAI.ai.react.tools;

import com.myAI.myAI.ai.react.ReactTool;
import org.springframework.stereotype.Component;

/**
 * 搜索工具实现
 */
@Component
public class SearchTool implements ReactTool {
    
    @Override
    public String getName() {
        return "SEARCH";
    }
    
    @Override
    public String getDescription() {
        return "搜索相关信息，可以搜索网络、数据库或其他来源的信息";
    }
    
    @Override
    public String execute(String input) {
        // TODO: 实现实际的搜索功能
        // 这里是示例实现
        return "搜索结果: 关于 '" + input + "' 的信息...";
    }
    
    @Override
    public String getParameterDescription() {
        return "搜索关键词或短语";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
} 