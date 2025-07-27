package com.myAI.myAI.ai.react.tools;

import com.myAI.myAI.ai.react.ReactTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.util.concurrent.*;

/**
 * 代码执行器工具
 * 支持执行简单的JavaScript代码片段
 */
@Component
@Slf4j
public class CodeExecutorTool implements ReactTool {
    
    private final ScriptEngineManager scriptEngineManager;
    private static final int EXECUTION_TIMEOUT_SECONDS = 10;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    
    public CodeExecutorTool() {
        this.scriptEngineManager = new ScriptEngineManager();
    }
    
    @Override
    public String getName() {
        return "CODE_EXECUTOR";
    }
    
    @Override
    public String getDescription() {
        return "执行简单的JavaScript代码片段，用于数据处理、逻辑计算等";
    }
    
    @Override
    public String execute(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return "错误：请提供要执行的代码";
            }
            
            String code = input.trim();
            log.info("执行代码: {}", code);
            
            // 安全检查
            if (!isSafeCode(code)) {
                return "错误：代码包含不安全的操作";
            }
            
            // 在独立线程中执行代码，设置超时
            Future<String> future = executorService.submit(() -> executeCode(code));
            
            try {
                String result = future.get(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.info("代码执行完成，结果: {}", result);
                return result;
            } catch (TimeoutException e) {
                future.cancel(true);
                return "错误：代码执行超时";
            } catch (ExecutionException e) {
                return "错误：代码执行失败 - " + e.getCause().getMessage();
            }
            
        } catch (Exception e) {
            log.error("代码执行过程发生错误", e);
            return "错误：执行失败 - " + e.getMessage();
        }
    }
    
    @Override
    public String getParameterDescription() {
        return "JavaScript代码片段，支持:\n" +
               "- 基本语法: 变量声明、函数定义、条件语句、循环\n" +
               "- 数据处理: 数组操作、字符串处理、对象操作\n" +
               "- 数学运算: Math对象的所有方法\n" +
               "- 输出: 使用 console.log() 或直接返回值\n" +
               "限制: 不支持文件操作、网络请求、系统调用\n" +
               "示例: 'let arr = [1,2,3]; arr.map(x => x*2)'";
    }
    
    @Override
    public boolean isAvailable() {
        return scriptEngineManager.getEngineByName("JavaScript") != null;
    }
    
    /**
     * 检查代码是否安全
     */
    private boolean isSafeCode(String code) {
        // 检查危险的关键词和操作
        String[] dangerousKeywords = {
            "import", "require", "eval", "Function",
            "System", "Runtime", "Process", "Thread",
            "File", "FileReader", "FileWriter",
            "XMLHttpRequest", "fetch", "WebSocket",
            "setTimeout", "setInterval",
            "java.", "javax.", "com.sun.",
            "Class.forName", "getClass", "newInstance"
        };
        
        String lowerCode = code.toLowerCase();
        for (String keyword : dangerousKeywords) {
            if (lowerCode.contains(keyword.toLowerCase())) {
                log.warn("代码包含危险关键词: {}", keyword);
                return false;
            }
        }
        
        // 检查代码长度
        if (code.length() > 5000) {
            log.warn("代码过长: {} 字符", code.length());
            return false;
        }
        
        return true;
    }
    
    /**
     * 执行JavaScript代码
     */
    private String executeCode(String code) throws ScriptException {
        ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");
        
        // 设置输出捕获
        StringWriter output = new StringWriter();
        engine.getContext().setWriter(output);
        
        // 添加console.log支持
        String wrappedCode = 
            "var console = { log: function() { " +
            "  for (var i = 0; i < arguments.length; i++) { " +
            "    print(arguments[i]); " +
            "    if (i < arguments.length - 1) print(' '); " +
            "  } " +
            "  print('\\n'); " +
            "}};\n" + code;
        
        try {
            Object result = engine.eval(wrappedCode);
            String outputStr = output.toString();
            
            StringBuilder response = new StringBuilder();
            
            // 添加控制台输出
            if (!outputStr.trim().isEmpty()) {
                response.append("输出:\n").append(outputStr.trim()).append("\n");
            }
            
            // 添加返回值
            if (result != null && !result.toString().equals("undefined")) {
                if (response.length() > 0) {
                    response.append("\n");
                }
                response.append("返回值: ").append(formatResult(result));
            }
            
            if (response.length() == 0) {
                response.append("代码执行完成，无输出");
            }
            
            return response.toString();
            
        } catch (ScriptException e) {
            throw new ScriptException("JavaScript执行错误: " + e.getMessage());
        }
    }
    
    /**
     * 格式化执行结果
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        
        if (result instanceof String) {
            return "\"" + result + "\"";
        }
        
        if (result instanceof Number) {
            return result.toString();
        }
        
        if (result instanceof Boolean) {
            return result.toString();
        }
        
        // 对于复杂对象，尝试转换为JSON格式
        try {
            return result.toString();
        } catch (Exception e) {
            return "[Object]";
        }
    }
}
