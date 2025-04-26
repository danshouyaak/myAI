package com.myAI.myAI.langchain.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

/**
 * funtioncall 工具类
 */

@Service
public class ToolsService {

    @Tool("今天几号")
    public String getCurrentDate(@P("日期") String name ) {
        System.out.println("日期：" + name);
        return "2023-07-01";
    }
}
