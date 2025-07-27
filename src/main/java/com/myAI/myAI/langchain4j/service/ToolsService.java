package com.myAI.myAI.langchain4j.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * funtioncall 工具类
 */

@Slf4j
public class ToolsService {

    @Tool("今天几号")
    public String getCurrentDate(@P("日期") String name) {
        System.out.println("日期：" + name);
        return "2023-07-01";
    }

    @Tool("去水印")
    public String getWatermark(@P("图片路径") String path) {
        log.info("图片路径：" + path);
        return "完成";
    }

    @Tool("天气查询")
    public String getWeather(@P("城市") String city) {
        // 1. 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build();

        // 2. 用 HttpUrl.Builder 安全地拼接参数（避免空格、中文乱码）
        HttpUrl url = new HttpUrl.Builder().scheme("https").host("v.api.aa1.cn").addPathSegment("api").addPathSegment("api-tianqi-3").addPathSegment("index.php").addQueryParameter("msg", city).addQueryParameter("type", String.valueOf(1)) // 中文自动 URL-Encode
                .build();


        // 3. 构造 GET Request
        Request request = new Request.Builder().url(url).get().build();

        // 4. 发送同步请求（生产环境请放到子线程或使用异步 enqueue）
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                log.info("请求成功 response {}", response);
                String body = response.body().string();
                System.out.println("==================" + body);
                return body;
            } else {
                System.err.println("HTTP 错误码: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "查询失败";
    }

}
