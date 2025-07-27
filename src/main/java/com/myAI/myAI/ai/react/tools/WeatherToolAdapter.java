package com.myAI.myAI.ai.react.tools;

import com.myAI.myAI.ai.react.ReactTool;
import com.myAI.myAI.langchain4j.tools.FindWeatherTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 天气查询工具适配器
 * 将LangChain4j的FindWeatherTool适配到ReAct系统
 */
@Component
@Slf4j
public class WeatherToolAdapter implements ReactTool {
    
    private final FindWeatherTool findWeatherTool;
    
    public WeatherToolAdapter() {
        this.findWeatherTool = new FindWeatherTool();
    }
    
    @Override
    public String getName() {
        return "WEATHER";
    }
    
    @Override
    public String getDescription() {
        return "天气查询工具，可以查询指定城市的天气信息";
    }
    
    @Override
    public String execute(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return "错误：请提供要查询的城市名称";
            }
            
            String city = input.trim();
            log.info("查询天气，城市: {}", city);
            
            String result = findWeatherTool.findWeather(city);
            
            if (result == null || result.equals("查询失败")) {
                return "天气查询失败，请检查城市名称是否正确或稍后重试";
            }
            
            // 格式化返回结果
            return formatWeatherResult(city, result);
            
        } catch (Exception e) {
            log.error("天气查询失败", e);
            return "天气查询失败: " + e.getMessage();
        }
    }
    
    @Override
    public String getParameterDescription() {
        return "城市名称，支持中文城市名\n" +
               "示例: '北京', '上海', '广州', '深圳'等\n" +
               "注意: 请提供准确的城市名称以获得最佳查询结果";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    /**
     * 格式化天气查询结果
     */
    private String formatWeatherResult(String city, String rawResult) {
        try {
            // 简单的结果格式化
            return String.format("🌤️ %s天气信息:\n%s", city, rawResult);
        } catch (Exception e) {
            log.warn("天气结果格式化失败", e);
            return rawResult;
        }
    }
}
