package com.myAI.myAI.controller;

import com.google.gson.Gson;
import com.myAI.myAI.common.AIModel;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.manager.AiManager;
import com.myAI.myAI.models.entity.Conversation;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.AIRequestVO;
import com.myAI.myAI.mq.MyMessageProducer;
import com.myAI.myAI.service.UserService;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.getUser;

// 这里不能使用 @RestController, 要用 @Controller
@RestController
@RequestMapping("Hello")
@Slf4j
public class AIChatController {
    @Resource
    private AiManager aiManager;

    @Resource
    private UserService userService;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * sse 流式调用
     *
     * @return
     */
    @GetMapping(value = "/GetHello/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter GetHelloStream(AIRequestVO requestVO, HttpServletRequest request) {
//        获取当前用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String content = requestVO.getContent();
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
//        模型id
        long modelId = requestVO.getModelId();
        if (modelId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型id不能为空");
        }
        log.info("请求内容：{}", content);
        // 建立 SSE 连接对象，0 表示永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 创建一个缓存变量来存储完整信息
        AtomicReference<StringBuilder> completeMessageBuilder = new AtomicReference<>(new StringBuilder());


        // AI 生成，SSE 流式返回
//        todo 这里需要改为让用户输入的  temperature 为 null 时，默认值为 0.99f


//        获取用户所需要的模型
        AIModel aiModel = new AIModel();
        HashMap<Long, String> userAIModel = aiModel.getAIModel();
        String aiModelDescribe = userAIModel.get(modelId);

        Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(aiModelDescribe, content, null);

        modelDataFlowable.observeOn(Schedulers.io()).doOnNext(character -> {
            log.info("sse 发送数据：{}", character);
            try {
                sseEmitter.send(character); // 发送数据到客户端
                String content1 = character.getChoices().get(0).getDelta().getContent();
                completeMessageBuilder.get().append(content1);
            } catch (IOException e) {
                sseEmitter.completeWithError(e); // 错误处理
            }
        }).doOnError((e) -> log.error("sse error", e)).doOnComplete(() -> {
            // 在流完成时获取完整信息
            String completeMessage = completeMessageBuilder.get().toString();
            log.info("完整信息：{}", completeMessage);


//                    封装用户发送信息
            Message userMsg = new Message();
            //         封装ai发送信息
            Message AIMsg = new Message();
            String conversationId = UUID.randomUUID().toString();
            userMsg.setConversationId(conversationId);
            userMsg.setMessageType("user");
            userMsg.setMessageContent(content);
            userMsg.setSendTime(new Date());

            AIMsg.setConversationId(conversationId);
            AIMsg.setMessageType("ai");
            AIMsg.setMessageContent(completeMessage);
            AIMsg.setSendTime(new Date());


            Gson gson = new Gson();
//            发送给mq
            myMessageProducer.sedMessage(gson.toJson(userMsg));
            myMessageProducer.sedMessage(gson.toJson(AIMsg));

            sseEmitter.complete();
        }).subscribe();


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
        redisTemplate.opsForValue().set("test", "hello");
        redisTemplate.opsForValue().set("key", "value");
        String value = redisTemplate.opsForValue().get("key");
        return value;
    }
}
