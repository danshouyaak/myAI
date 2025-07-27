package com.myAI.myAI.langchain4j.service;

import com.myAI.myAI.config.LangChainConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
class ToolsServiceTest {
    @Resource
    private LangChainConfig.AssistantUnique assistantUnique;

    @Test
    public void getCurrentDate() {
        log.info("=================================================");
        String s = assistantUnique.chatTest("今天几号");
        log.info(s);
        System.out.println(s);

    }

    @Test
    void getWeather() throws IOException {
//         创建 OkHttpClient 实例 并设置超时时间
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request build = new Request.Builder()
                .url("https://whyta.cn/api/tianqi?key=d5c296b91907&city=武汉")
                .build();

        Response execute = client.newCall(build).execute();
        log.info("execute:{}",execute.body());
    }
}