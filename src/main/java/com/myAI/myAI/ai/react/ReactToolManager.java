package com.myAI.myAI.ai.react;

import com.myAI.myAI.ai.react.tools.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReAct工具管理器
 * 负责管理和调用所有可用的工具
 */
@Component
@Slf4j
public class ReactToolManager {
    
    private final Map<String, ReactTool> toolMap = new HashMap<>();

    @Resource
    private SearchTool searchTool;

    @Resource
    private McpWebSearchTool mcpWebSearchTool;

    @Resource
    private CalculatorTool calculatorTool;

    @Resource
    private CodeExecutorTool codeExecutorTool;

    @Resource
    private TextProcessorTool textProcessorTool;

    @Resource
    private DateTimeTool dateTimeTool;

    @Resource
    private WeatherToolAdapter weatherToolAdapter;

    @Resource
    private WebSearchToolAdapter webSearchToolAdapter;

    @Resource
    private TerminateToolAdapter terminateToolAdapter;
    
    /**
     * 初始化：注册所有工具
     */
    @PostConstruct
    public void init() {
        // 注册搜索工具
        registerTool(searchTool);

        // 注册MCP Web搜索工具
        registerTool(mcpWebSearchTool);

        // 注册计算器工具
        registerTool(calculatorTool);

        // 注册代码执行器工具
        registerTool(codeExecutorTool);

        // 注册文本处理工具
        registerTool(textProcessorTool);

        // 注册时间日期工具
        registerTool(dateTimeTool);

        // 注册LangChain4j适配器工具
        registerTool(weatherToolAdapter);
        registerTool(webSearchToolAdapter);

        // 注册终止工具
        registerTool(terminateToolAdapter);

        log.info("已注册 {} 个工具", toolMap.size());
        toolMap.forEach((name, tool) ->
            log.info("工具: {} - {}", name, tool.getDescription()));
    }
    
    /**
     * 注册工具
     */
    public void registerTool(ReactTool tool) {
        if (tool != null && tool.isAvailable()) {
            toolMap.put(tool.getName(), tool);
        }
    }
    
    /**
     * 获取工具
     */
    public ReactTool getTool(String name) {
        return toolMap.get(name);
    }
    
    /**
     * 获取所有可用工具的描述
     */
    public String getAllToolsDescription() {
        return toolMap.values().stream()
            .map(tool -> String.format(
                "Tool: %s\nDescription: %s\nParameters: %s\n",
                tool.getName(),
                tool.getDescription(),
                tool.getParameterDescription()
            ))
            .collect(Collectors.joining("\n"));
    }
    
    /**
     * 执行工具
     */
    public String executeTool(String toolName, String input) {
        ReactTool tool = getTool(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }
        return tool.execute(input);
    }
    
    /**
     * 获取所有可用工具列表
     */
    public List<ReactTool> getAllTools() {
        return toolMap.values().stream()
            .filter(ReactTool::isAvailable)
            .collect(Collectors.toList());
    }

    /**
     * 检查工具是否可用
     */
    public boolean isToolAvailable(String toolName) {
        ReactTool tool = getTool(toolName);
        return tool != null && tool.isAvailable();
    }

    /**
     * 获取工具数量
     */
    public int getToolCount() {
        return (int) toolMap.values().stream()
            .filter(ReactTool::isAvailable)
            .count();
    }

    /**
     * 获取所有工具名称列表
     */
    public List<String> getAllToolNames() {
        return toolMap.values().stream()
            .filter(ReactTool::isAvailable)
            .map(ReactTool::getName)
            .collect(Collectors.toList());
    }
}