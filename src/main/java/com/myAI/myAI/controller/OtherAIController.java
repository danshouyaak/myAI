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
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
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

    @GetMapping(value = "/stream_chat",produces ="text/stream;charset=UTF-8")
    public Flux<String> GetHello2(@RequestParam String content, @RequestParam(defaultValue = "1") Long memoryId) {
        TokenStream stream = assistantUnique.stream(String.valueOf(memoryId), content);
        return Flux.create(sink -> {
            stream.onPartialResponse(sink::next)
                    .onCompleteResponse(c -> {sink.complete();})
                    .onError(sink::error)
                    .start();
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


    @GetMapping(value = "/sse",produces =  MediaType.TEXT_EVENT_STREAM_VALUE)
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