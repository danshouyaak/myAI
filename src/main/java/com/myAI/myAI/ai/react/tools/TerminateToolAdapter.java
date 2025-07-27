package com.myAI.myAI.ai.react.tools;

import com.myAI.myAI.ai.react.ReactTool;
import com.myAI.myAI.langchain4j.tools.TerminateTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 终止工具适配器
 * 用于结束 ReAct 思考循环
 */
@Component
@Slf4j
public class TerminateToolAdapter implements ReactTool {

    @Resource
    private TerminateTool terminateTool;

    @Override
    public String getName() {
        return "TERMINATE";
    }

    @Override
    public String getDescription() {
        return "终止当前思考过程并给出最终答案。当你已经得到了问题的答案或完成了所有必要的任务时，使用此工具结束对话。";
    }

    @Override
    public String getParameterDescription() {
        return "final_answer: 最终答案或结论";
    }

    @Override
    public String execute(String input) {
        log.info("执行终止工具，最终答案: {}", input);
        
        // 调用原始的 TerminateTool
        String result = terminateTool.doTerminate();
        
        // 返回最终答案
        return String.format("FINAL_ANSWER: %s", input);
    }

    @Override
    public boolean isAvailable() {
        return terminateTool != null;
    }
}
