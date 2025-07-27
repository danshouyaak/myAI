package com.myAI.myAI.config;

import com.myAI.myAI.langchain4j.PersistentChatMemoryStore;
import com.myAI.myAI.langchain4j.service.ToolsService;
import com.myAI.myAI.langchain4j.tools.FindWeatherTool;
import com.myAI.myAI.langchain4j.tools.ToolRegistration;
import com.myAI.myAI.langchain4j.tools.WebSearchTool;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@Configuration
public class LangChainConfig {
    // 非记忆版
    @Bean
    public Assistant assistant(@Qualifier("reactQwenChatModel") ChatLanguageModel qwenChatModel,
                              @Qualifier("reactQwenStreamingChatModel") StreamingChatLanguageModel qwenStreamingChatModel) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .build();
        return assistant;
    }

    @Resource
    private RedisTemplate<String, String> redisTemplate;

//    创建一个向量数据库
    @Bean
    public EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore();
    }

    @Bean("reactQwenChatModel")
    public ChatLanguageModel reactQwenChatModel() {
        return QwenChatModel.builder()
                .apiKey("sk-83365e2d612a4576b14ba1f823af2b10")
                .modelName("qwen-plus")
                .temperature(0.7F)
                .build();
    }

    @Bean("reactQwenStreamingChatModel")
    public StreamingChatLanguageModel reactQwenStreamingChatModel() {
        return QwenStreamingChatModel.builder()
                .apiKey("sk-83365e2d612a4576b14ba1f823af2b10")
                .modelName("qwen-plus")
                .temperature(0.7F)
                .build();
    }


//    质谱ai
//    @Bean
//    public ZhipuAiStreamingChatModel zhipuAiStreamingChatModel() {
//        return  ZhipuAiStreamingChatModel.builder()
//                .apiKey("7d87de8424d64f239da60fcd2fbf8ea8.mhKGQxgcVD21XoSW")
////                .model("glm-4")  // 需要注释不然会有bug
//                .logRequests(true)
//                .logResponses(true)
//                .callTimeout(Duration.ofSeconds(60))
//                .connectTimeout(Duration.ofSeconds(60))
//                .writeTimeout(Duration.ofSeconds(60))
//                .readTimeout(Duration.ofSeconds(60))
//                .build();
//    }


    // 记忆版
    @Bean
    public AssistantUnique assistantUniqueStore(@Qualifier("reactQwenChatModel") ChatLanguageModel qwenChatModel,
                                                @Qualifier("reactQwenStreamingChatModel") StreamingChatLanguageModel qwenStreamingChatModel,
                                                EmbeddingStore embeddingStore,
                                                QwenEmbeddingModel qwenEmbeddingModel) {

        PersistentChatMemoryStore store = new PersistentChatMemoryStore(redisTemplate);

//        内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)  // 绑定向量数据库
                .embeddingModel(qwenEmbeddingModel)   // 绑定向量模型
                .maxResults(5) // 最相似的5个结果
                .minScore(0.6) // 只找相似度在0.6以上的内容
                .build();


        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();


        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
//                .toolProvider(toolProvider)
                .tools(new ToolsService())
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(contentRetriever)  // 绑定内容检索器
                .build();
        return assistant;
    }


    /**
     * 测试 功能
     */
    static final String SystemPrompt1 =
        "# AI助手系统提示词\n\n" +
        "## 角色定义\n" +
        "你是一个高级智能助手，具备多步骤推理和工具调用能力。必须遵循以下处理流程：\n" +
        "1. **理解需求** → 2. **分步推理** → 3. **工具调用** → 4. **验证结论** → 5. **结构化输出**\n\n" +
        "## 核心能力规范\n\n" +
        "### 1. 推理过程（必须展示思考链）\n" +
        "```reasoning\n" +
        "→ 步骤1: [问题类型识别]\n" +
        "   • 用户意图分类（信息查询/计算/创作等）\n" +
        "   • 关键需求提取\n\n" +
        "→ 步骤2: [解决方案设计]\n" +
        "   • 是否需要外部数据/工具\n" +
        "   • 潜在路径评估\n\n" +
        "→ 步骤3: [约束条件分析]\n" +
        "   • 时间/空间复杂度考量\n" +
        "   • 可行性验证\n\n" +
        "   {\n" +
        "     \"action\": \"WebSearch\",\n" +
        "     \"params\": {\n" +
        "       \"Search query keyword\": \"值1\"\n" +
        "     },\n" +
        "     \"reason\": \"调用理由说明\"\n" +
        "   }\n\n" +
        "### 2. 工具调用\n" +
        "    -调用了某某工具完成了某件事\n" +
        "    -调用了某某工具完成了某件事\n" +
        "    -调用了某某工具完成了某件事\n\n" +
        "   **核心结论**：[首句概括答案]\n" +
        "   **推理过程**：\n" +
        "     1. 关键推理步骤1\n" +
        "     2. 关键推理步骤2\n" +
        "   **支持证据**：\n" +
        "     • 数据点1 [来源]\n" +
        "     • 数据点2 [来源]\n" +
        "   **不确定性说明**：[如有]";

    static final String SystemPrompt2 =
        "你是一个专业的问题解决助手，必须严格遵循 **ReAct 框架** 工作：\n" +
        "1. **思考(Thought)**：分析问题本质，决定是否需要工具\n" +
        "2. **执行(Action)**：调用工具（如需）\n" +
        "3. **观察(Observation)**：分析工具返回结果\n" +
        "4. **结论(Answer)**：基于观察给出最终答案\n\n" +
        "### 工具使用规则：\n" +
        "1. 当需要实时数据、计算或专业能力时调用工具\n" +
        "2. 每次只调用一个工具，严格按JSON格式：\n" +
        "```json\n" +
        "{\"action\": \"工具名\", \"params\": {\"参数\": \"值\"}}\n" +
        "```";
    static final String SystemPrompt3 =
        "你是一个问题解决助手，使用 ReAct 框架分步处理问题：\n" +
        "步骤：\n" +
        "- Thought: 分析问题，决定是否使用工具\n" +
        "- Action: 调用工具（格式：{\"action\":\"工具名\", \"params\":{\"参数\":\"值\"}}）\n" +
        "- Observation: 工具返回的结果\n" +
        "重复直到得出最终答案，以 \"Final Answer:\" 开头输出结论。\n\n" +
        "可用工具：\n" +
        "- weather_tool: 查询天气，参数：city（城市名）\n" +
        "- math_calculator: 数学计算，参数：expression（表达式）\n\n" +
        "示例：\n" +
        "用户：北京今天气温多少度？\n" +
        "Thought: 需要查询北京天气\n" +
        "Action: {\"action\":\"weather_tool\",\"params\":{\"city\":\"北京\"}}\n" +
        "Observation: {\"temperature\": 28, \"condition\": \"Sunny\"}\n" +
        "Final Answer: 北京今天28°C，晴天。";

    public interface AssistantTest {
        @SystemMessage(SystemPrompt3)
        String chat(@UserMessage String message);

        // 流式响应
        TokenStream stream(@UserMessage String message);
    }
    @Bean
    public AssistantTest assistantTest(ToolRegistration toolsRegister) {
        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey("sk-83365e2d612a4576b14ba1f823af2b10")
                .modelName("qwen-plus")
                .temperature(0.7F)
                .build();
      return   AiServices.builder(LangChainConfig.AssistantTest.class)
                .chatLanguageModel(qwenChatModel)
                .tools(new FindWeatherTool(), new WebSearchTool(), new ToolsService())
                .build();
    }


    public interface Assistant {
        String chat(String message);

        // 流式响应
        TokenStream stream(String message);
    }

    public interface AssistantUnique {

//        用来测试功能
        String chatTest(@UserMessage String userMessage);

        String chat(@MemoryId String memoryId, @UserMessage String userMessage);

        @SystemMessage("这是你的的角色 请严格遵守{{description}}")
            // 流式响应
        TokenStream stream(@MemoryId String memoryId, @UserMessage String userMessage, @V("description") String description);

        TokenStream stream1(@MemoryId String memoryId, @UserMessage String userMessage);
    }
}
