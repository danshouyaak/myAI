package com.myAI.myAI.ai.react.tools;

import com.myAI.myAI.ai.react.ReactTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * 计算器工具实现
 * 支持基本的数学运算，包括加减乘除、幂运算、三角函数等
 */
@Component
@Slf4j
public class CalculatorTool implements ReactTool {
    
    private final ScriptEngine scriptEngine;
    private static final Pattern SAFE_EXPRESSION_PATTERN = Pattern.compile("^[0-9+\\-*/().\\s\\^sincostan\\w]*$");
    
    public CalculatorTool() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.scriptEngine = manager.getEngineByName("JavaScript");
    }
    
    @Override
    public String getName() {
        return "CALCULATOR";
    }
    
    @Override
    public String getDescription() {
        return "执行数学计算，支持基本运算(+,-,*,/)、幂运算(^)、三角函数(sin,cos,tan)、对数函数等";
    }
    
    @Override
    public String execute(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return "错误：请提供要计算的数学表达式";
            }
            
            String expression = input.trim();
            log.info("计算表达式: {}", expression);
            
            // 安全检查
            if (!isSafeExpression(expression)) {
                return "错误：表达式包含不安全的字符或函数";
            }
            
            // 预处理表达式
            String processedExpression = preprocessExpression(expression);
            
            // 执行计算
            Object result = scriptEngine.eval(processedExpression);
            
            if (result == null) {
                return "错误：计算结果为空";
            }
            
            // 格式化结果
            String formattedResult = formatResult(result);
            log.info("计算结果: {} = {}", expression, formattedResult);
            
            return String.format("计算结果: %s = %s", expression, formattedResult);
            
        } catch (ScriptException e) {
            log.error("计算表达式失败: {}", e.getMessage());
            return "错误：无效的数学表达式 - " + e.getMessage();
        } catch (Exception e) {
            log.error("计算过程发生错误", e);
            return "错误：计算失败 - " + e.getMessage();
        }
    }
    
    @Override
    public String getParameterDescription() {
        return "数学表达式，支持:\n" +
               "- 基本运算: +, -, *, /\n" +
               "- 幂运算: ^ 或 **\n" +
               "- 括号: ()\n" +
               "- 三角函数: sin(), cos(), tan()\n" +
               "- 对数函数: log(), log10()\n" +
               "- 常数: PI, E\n" +
               "示例: '2 + 3 * 4', 'sin(PI/2)', 'log(10)'";
    }
    
    @Override
    public boolean isAvailable() {
        return scriptEngine != null;
    }
    
    /**
     * 检查表达式是否安全
     */
    private boolean isSafeExpression(String expression) {
        // 基本安全检查，防止恶意代码执行
        if (expression.contains("import") || 
            expression.contains("eval") || 
            expression.contains("function") ||
            expression.contains("var ") ||
            expression.contains("let ") ||
            expression.contains("const ") ||
            expression.contains("while") ||
            expression.contains("for") ||
            expression.contains("if") ||
            expression.contains("System") ||
            expression.contains("Runtime")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 预处理表达式，转换为JavaScript可执行的格式
     */
    private String preprocessExpression(String expression) {
        // 替换数学常数
        expression = expression.replaceAll("\\bPI\\b", "Math.PI");
        expression = expression.replaceAll("\\bE\\b", "Math.E");
        
        // 替换幂运算符
        expression = expression.replaceAll("\\^", "**");
        
        // 替换数学函数
        expression = expression.replaceAll("\\bsin\\(", "Math.sin(");
        expression = expression.replaceAll("\\bcos\\(", "Math.cos(");
        expression = expression.replaceAll("\\btan\\(", "Math.tan(");
        expression = expression.replaceAll("\\blog\\(", "Math.log(");
        expression = expression.replaceAll("\\blog10\\(", "Math.log10(");
        expression = expression.replaceAll("\\bsqrt\\(", "Math.sqrt(");
        expression = expression.replaceAll("\\babs\\(", "Math.abs(");
        expression = expression.replaceAll("\\bfloor\\(", "Math.floor(");
        expression = expression.replaceAll("\\bceil\\(", "Math.ceil(");
        expression = expression.replaceAll("\\bround\\(", "Math.round(");
        
        return expression;
    }
    
    /**
     * 格式化计算结果
     */
    private String formatResult(Object result) {
        if (result instanceof Number) {
            double value = ((Number) result).doubleValue();
            
            // 检查是否为整数
            if (value == Math.floor(value) && !Double.isInfinite(value)) {
                return String.valueOf((long) value);
            } else {
                // 保留适当的小数位数
                BigDecimal bd = BigDecimal.valueOf(value);
                bd = bd.setScale(10, RoundingMode.HALF_UP);
                // 移除尾随零
                bd = bd.stripTrailingZeros();
                return bd.toPlainString();
            }
        }
        
        return result.toString();
    }
}
