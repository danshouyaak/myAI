package com.myAI.myAI.controller;

import cn.hutool.core.util.StrUtil;
import com.myAI.myAI.manager.AiManager;
import com.myAI.myAI.model.vo.AIRequestVO;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 这里不能使用 @RestController, 要用 @Controller
@RestController
@RequestMapping("Hello")
@Slf4j
public class AIChatController {
    @Resource
    private AiManager aiManager;

    /**
     * sse 流式调用
     *
     * @return
     */
    @GetMapping(value = "/GetHello/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter GetHelloStream(AIRequestVO requestVO) {
        String content = requestVO.getContent();
        if (StringUtils.isBlank(content)) {
            throw new RuntimeException("内容不能为空");
        }

        log.info("请求内容：{}", content);
        // 建立 SSE 连接对象，0 表示永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);
        // AI 生成，SSE 流式返回
//        todo 这里需要改为让用户输入的  temperature 为 null 时，默认值为 0.99f
        Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest("你是一名中医", content, null);
        modelDataFlowable.observeOn(Schedulers.io()).map(modelData -> modelData.getChoices().get(0).getDelta().getContent())
//                把空格替换成空
                .map(message -> message.replaceAll("\\s", ""))
//                判空处理
                .filter(StrUtil::isNotBlank).flatMap(message -> {
                    List<Character> characterList = new ArrayList<>();
//            字符处理
                    for (char c : message.toCharArray()) {
                        characterList.add(c);
                    }
                    return Flowable.fromIterable(characterList);
                }).doOnNext(character -> {
                    log.info("sse 发送数据：{}", character);
                    try {
                        sseEmitter.send(character.toString()); // 发送数据到客户端
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e); // 错误处理
                    }
                }).doOnError((e) -> log.error("sse error", e)).doOnComplete(sseEmitter::complete).subscribe();
        return sseEmitter;
    }

    @GetMapping(value = "/GetHellos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void GetHellos(HttpServletResponse response) throws Exception {
        if (response.containsHeader("Content-Type")) {
            response.setHeader("Content-Type", "text/event-stream");
        } else {
            response.setHeader("Content-Type", "text/event-stream");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
        }
        String data = "id:" + new Random().nextInt() + " \n" + "retry: " + new Random().nextInt() * 30 + "\n" + "event: message\n" + "data: " + new Random().nextInt() + "\n\n";
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(data);
    }

    @GetMapping("/test")
    public String GetHello() {
        return "hello";
    }
}
