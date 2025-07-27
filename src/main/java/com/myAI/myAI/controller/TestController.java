package com.myAI.myAI.controller;

import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.config.LangChainConfig;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Resource
    private LangChainConfig.AssistantUnique assistantUnique;

    @GetMapping("/test1")
//    @RequestParam(defaultValue = "你是谁")
    public String test(@RequestParam(defaultValue = "你好今天是几号") String text) {
        return assistantUnique.chatTest(text);
    }

    /**
     * 测试接口
     * 1. 测试接口是否正常
     * 2. 测试接口是否可以调用第三方接口
     * 3. 测试接口是否可以调用第三方接口，并且返回数据
     * 4. 测试接口是否可以调用第三方接口，并且返回数据，并且返回数据的格式是json
     * 5. 测试接口是否可以调用第三方接口，并且返回数据，并且返回数据的格式是json，并且返回数据的格式是json
     * 6. 测试接口是否可以调用第三方接口，并且返回数据，并且返回数据的格式是json，并且返回数据的格式是json，并且返回数据的格式是json
     * @param text
     * @return
     * @throws IOException
     */
//    @GetMapping(value = "/test2")
    public BaseResponse<String> test2(@RequestParam(defaultValue = "你好今天是几号") String text) throws IOException {
        // 1. 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build();

        // 2. 用 HttpUrl.Builder 安全地拼接参数（避免空格、中文乱码）
        HttpUrl url = new HttpUrl.Builder().scheme("https").host("whyta.cn").addPathSegment("api").addPathSegment("tianqi").addQueryParameter("key", "d5c296b91907").addQueryParameter("city", "武汉")   // 中文自动 URL-Encode
                .build();

        // 3. 构造 GET Request
        Request request = new Request.Builder().url(url).get().build();

        // 4. 发送同步请求（生产环境请放到子线程或使用异步 enqueue）
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                log.info("请求成功 response {}", response);
                String body = response.body().string();
                System.out.println("=================="+body);
                return ResultUtils.success(body);
            } else {
                System.err.println("HTTP 错误码: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * web搜索
     */


    @Resource
    LangChainConfig.AssistantTest AssistantTest;

    @GetMapping(value = "/test3")
    public BaseResponse<String> test3(@RequestParam(defaultValue = "上网分析一下最近的ai趋势") String text) throws IOException {

        String chat = AssistantTest.chat(text);
        log.info("chat {}", chat);
        return ResultUtils.success(chat);
    }

}
