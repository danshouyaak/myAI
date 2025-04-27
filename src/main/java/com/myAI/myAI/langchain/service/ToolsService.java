package com.myAI.myAI.langchain.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.mcp.McpToolProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * funtioncall 工具类
 */

public class ToolsService {


    @Tool("今天几号")
    public String getCurrentDate(@P("日期") String name ) {
        System.out.println("日期：" + name);
        return "2023-07-01";
    }

//    @Tool("路线")
//    public String getMap(@P("路线") String name) {
//        System.out.println("星期几：" + name);
//        return "星期二";
//    }
}
