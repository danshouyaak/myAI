package com.myAI.myAI;

import com.myAI.myAI.controller.AIChatController;
import com.myAI.myAI.manager.AiManager;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ZhiPuAiTest {

    @Resource
    private AiManager aiManager;

    @Test
    public void testSyncRequest() {
        String systemMessage = "作为一名营销专家，请为智谱开放平台创作一个吸引人的slogan";
        String userMessage = "请为智谱开放平台创作一个吸引人的slogan";
        String result = aiManager.doSyncRequest(systemMessage, userMessage, 0.7f);
        System.out.println(result);
    }
    @Test
    public void testSSERequest() {
        String systemMessage = "作为一名营销专家，请为智谱开放平台创作一个吸引人的slogan";
        String userMessage = "请为智谱开放平台创作一个吸引人的slogan";
        Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(systemMessage, userMessage, 0.7f);
        System.out.println(modelDataFlowable.subscribe(System.out::println));
    }

    @Test
    public void test() {
        String apiKey = "e6b5fa664ff13a58f900355e8759b8d3.zX7Gif59H4yDbY9a";
        // 创建客户端
        ClientV4 client = new ClientV4.Builder(apiKey).build();
        // 构造请求
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), "作为一名营销专家，请为智谱开放平台创作一个吸引人的slogan");
        messages.add(chatMessage);
        String requestId = String.valueOf(System.currentTimeMillis());
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        // 调用
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        System.out.println("model output:" + invokeModelApiResp.getMsg());
    }

    @Test
    void rxJavaDemo() throws InterruptedException {
        // 创建一个流，每秒发射一个递增的整数（数据流变化）
        Flowable<Long> flowable = Flowable.interval(1, TimeUnit.SECONDS)
                .map(i -> i + 1)
                .subscribeOn(Schedulers.io()); // 指定创建流的线程池

        // 订阅 Flowable 流，并打印每个接受到的数字
        flowable.observeOn(Schedulers.io())
                .doOnNext(item -> System.out.println(item.toString()))
                .subscribe();

        // 让主线程睡眠，以便观察输出
        Thread.sleep(10000L);
    }


}
