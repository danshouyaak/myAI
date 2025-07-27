package com.myAI.myAI.langchain4j.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class ToolRegistration {
    @Bean
    public List<Object> toolsRegister() {
        ArrayList<Object> allTool = new ArrayList<>();
        TerminateTool terminateTool = new TerminateTool();
        FindWeatherTool findWeatherTool = new FindWeatherTool();
        allTool.addAll(Arrays.asList(terminateTool, findWeatherTool));
        return allTool;
    }
}
