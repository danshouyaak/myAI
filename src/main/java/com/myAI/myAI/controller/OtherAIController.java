package com.myAI.myAI.controller;


import com.google.gson.Gson;
import com.myAI.myAI.common.AIModel;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.config.LangChainConfig;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.manager.AiManager;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.AIRequestVO;
import com.zhipu.oapi.service.v4.model.ModelData;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.reactivex.Flowable;
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
@RestController
@RequestMapping("/ai_other")
public class OtherAIController {

    @Resource
    private StreamingChatLanguageModel qwenStreamingChatModel;

    @Resource
    private LangChainConfig.Assistant assistant;
    @Resource
    private LangChainConfig.AssistantUnique assistantUnique;

    /**
     * sse
     */
    @Resource
    private AiManager aiManager;


    /**
     * 匹配向量
     *
     * @param content
     * @param memoryId
     * @return
     */
    @GetMapping(value = "/stream_chat", produces = "text/stream;charset=UTF-8")
    public Flux<String> GetHello2(@RequestParam(defaultValue = "你是谁") String content, @RequestParam(defaultValue = "1") Long memoryId) {

        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder().apiKey("sk-83365e2d612a4576b14ba1f823af2b10").build();


        // 利用向量模型进行向量化， 然后存储向量到向量数据库
        TextSegment segment1 = TextSegment.from("       预订航班:\n" + "                - 通过我们的网站或移动应用程序预订。\n" + "                - 预订时需要全额付款。\n" + "                - 确保个人信息（姓名、ID 等）的准确性，因为更正可能会产生 25 的费用。");
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);


        // 利用向量模型进行向量化， 然后存储向量到向量数据库
        TextSegment segment2 = TextSegment.from(" 取消预订:\n" + "                - 最晚在航班起飞前 48 小时取消。\n" + "                - 取消费用：经济舱 75 美元，豪华经济舱 50 美元，商务舱 25 美元。\n" + "                - 退款将在 7 个工作日内处理。");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);

        // 需要查询的内容 向量化
        Embedding queryEmbedding = embeddingModel.embed("退票要多少钱").content();

        // 去向量数据库查询
        // 构建查询条件
        EmbeddingSearchRequest build = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).maxResults(1).build();

        // 查询
        EmbeddingSearchResult<TextSegment> segmentEmbeddingSearchResult = embeddingStore.search(build);
        segmentEmbeddingSearchResult.matches().forEach(embeddingMatch -> {
            System.out.println(embeddingMatch.score()); // 0.8144288515898701
            System.out.println(embeddingMatch.embedded().text()); // I like football

        });


//        McpTransport transport = new StdioMcpTransport.Builder()



        TokenStream stream = assistantUnique.stream(String.valueOf(memoryId), content, "你是一个人工智能名字叫小廖");
        return Flux.create(sink -> {
            stream.onPartialResponse(sink::next).onCompleteResponse(c -> {
                sink.complete();
            }).onError(sink::error).start();
        });
    }


//        return Flux.create(sink -> {
//            qwenStreamingChatModel.chat(message, new StreamingChatResponseHandler() {
//                @Override
//                public void onPartialResponse(String partialResponse) {
//                    sink.next(partialResponse);  // 逐次返回部分响应
//                }
//
//                @Override
//                public void onCompleteResponse(ChatResponse completeResponse) {
//                    sink.complete();  // 完成整个响应流
//                }
//
//                @Override
//                public void onError(Throwable error) {
//                    sink.error(error);  // 异常处理
//                }
//            });
//        });
//    }


    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter GetHelloStream(@RequestParam(defaultValue = "你是谁") String content, @RequestParam(defaultValue = "1") Long memoryId) {
//
        log.info("请求内容 sse：{}", content);
        // 建立 SSE 连接对象，0 表示永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 创建一个缓存变量来存储完整信息
        AtomicReference<StringBuilder> completeMessageBuilder = new AtomicReference<>(new StringBuilder());

        // AI 生成，SSE 流式返回
//        todo 这里需要改为让用户输入的  temperature 为 null 时，默认值为 0.99f

//        获取用户所需要的模型
        AIModel aiModel = new AIModel();
        HashMap<Long, String> userAIModel = aiModel.getAIModel();
        String aiModelDescribe = userAIModel.get(memoryId);

        Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(aiModelDescribe, content, null);

        modelDataFlowable.observeOn(Schedulers.io()).doOnNext(character -> {
            log.info("sse 发送数据：{}", character);
            try {
                String content1 = character.getChoices().get(0).getDelta().getContent();
                sseEmitter.send(content1); // 发送数据到客户端

                completeMessageBuilder.get().append(content1);
            } catch (IOException e) {
                sseEmitter.completeWithError(e); // 错误处理
            }
        }).doOnError((e) -> log.error("sse error", e)).doOnComplete(() -> {
        }).subscribe();
        return sseEmitter;
    }

}