package com.myAI.myAI.controller;


import com.myAI.myAI.config.LangChainConfig;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import io.reactivex.functions.Cancellable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.Resource;
import java.util.function.Supplier;

@RestController
@RequestMapping("/ai_other")
public class OtherAIController {

    @Resource
    private StreamingChatLanguageModel qwenStreamingChatModel;

    @Resource
    private LangChainConfig.Assistant assistant;
    @Resource
    private LangChainConfig.AssistantUnique assistantUnique;

    @GetMapping(value = "/stream_chat",produces ="text/stream;charset=UTF-8")
    public Flux<String> GetHello2(@RequestParam(defaultValue = "你是谁") String message, @RequestParam(defaultValue = "1") Long memoryId) {
        TokenStream stream = assistantUnique.stream(memoryId, message);

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
}