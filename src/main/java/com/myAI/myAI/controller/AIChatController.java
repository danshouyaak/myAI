package com.myAI.myAI.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.google.gson.Gson;
import com.myAI.myAI.common.AIModel;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.config.LangChainConfig;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.manager.AiManager;

import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.models.vo.AIRequestVO;
import com.myAI.myAI.mq.MyMessageProducer;
import com.myAI.myAI.service.UserService;
import com.zhipu.oapi.service.v4.model.ModelData;


import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import io.lettuce.core.pubsub.PubSubOutput;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;
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

    @Resource
    private LangChainConfig.AssistantUnique assistantUnique;

    /**
     * sse 流式调用
     *
     * @return
     */
    @GetMapping(value = "/GetHello/sse", produces = "text/stream;charset=UTF-8")
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

        String conversationId = requestVO.getConversationId();
        if (StringUtils.isBlank(conversationId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话id不能为空");
        }
//        模型id
        long modelId = requestVO.getModelId();
        if (modelId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型id不能为空");
        }
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
            userMsg.setConversationId(conversationId);
            userMsg.setMessageType("user");
            userMsg.setMessageContent(content);
            userMsg.setSendTime(new Date());
            userMsg.setAiId(requestVO.getModelId());

            AIMsg.setConversationId(conversationId);
            AIMsg.setMessageType("ai");
            AIMsg.setMessageContent(completeMessage);
            AIMsg.setSendTime(new Date());
            AIMsg.setAiId(requestVO.getModelId());


            Gson gson = new Gson();
//            发送给mq
            myMessageProducer.sedMessage(gson.toJson(userMsg));
            myMessageProducer.sedMessage(gson.toJson(AIMsg));
            sseEmitter.complete();
        }).subscribe();


        return sseEmitter;
    }

    /**
     * 流式回复
     *
     * @param request
     * @param modelId
     * @param content
     * @param conversationId
     * @return
     */

    @GetMapping(value = "/stream", produces = "text/stream;charset=UTF-8")
    public Flux<String> StreamChat(String content, long modelId, String conversationId, HttpServletRequest request) {
        //        获取当前用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
//        String content = requestVO.getContent();
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
//        会话id
//        String conversationId = requestVO.getConversationId();
        if (StringUtils.isBlank(conversationId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话id不能为空");
        }
//        模型id
//        long modelId = requestVO.getModelId();
        if (modelId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型id不能为空");
        }


        Long userId = loginUser.getId();  // 获取当前用户ID 记忆id
//        String message = requestVO.getContent();  // 获取用户输入的消息
        log.info("请求内容stream：{}", content);

        // 创建一个缓存变量来存储完整信息
        AtomicReference<StringBuilder> completeMessageBuilder = new AtomicReference<>(new StringBuilder());


//        获取ai预设信息
        AIModel aiModel = new AIModel();
        String modelAIModelDescription = aiModel.getAIModel(modelId);
        System.out.println(modelAIModelDescription);

        TokenStream stream = assistantUnique.stream(conversationId, content,modelAIModelDescription);
        return Flux.create(sink -> {
            stream.onPartialResponse(partialResponse -> {
                System.out.println("partialResponse:" + partialResponse);
                completeMessageBuilder.get().append(partialResponse);
                sink.next(partialResponse);
            });
            stream.onCompleteResponse(response -> {
//                发送完成

                // 在流完成时获取完整信息
                String completeMessage = completeMessageBuilder.get().toString();
                log.info("完整信息：{}", completeMessage);

//                    封装用户发送信息
                Message userMsg = new Message();
                //         封装ai发送信息
                Message AIMsg = new Message();
                userMsg.setConversationId(conversationId);
                userMsg.setMessageType("user");
                userMsg.setMessageContent(content);
                userMsg.setSendTime(new Date());
                userMsg.setAiId(modelId);

                AIMsg.setConversationId(conversationId);
                AIMsg.setMessageType("ai");
                AIMsg.setMessageContent(completeMessage);
                AIMsg.setSendTime(new Date());
                AIMsg.setAiId(modelId);

                Gson gson = new Gson();
//            发送给mq
                myMessageProducer.sedMessage(gson.toJson(userMsg));
                myMessageProducer.sedMessage(gson.toJson(AIMsg));
                sink.complete();
                System.out.println("完成");
            });
            stream.onError(error -> {
                System.out.println("错误");
            });
            stream.start();
        });
    }


    @GetMapping("/test")
    public Date GetHello() {
        // 获取当前时间并格式化为目标格式
        DateTime now = DateUtil.date();
        String formattedDate = DateUtil.format(now, "MMM dd, yyyy hh:mm:ss a");

        // 输出格式化后的字符串
        System.out.println(formattedDate);

        // 如果需要将格式化后的字符串重新转换为 Date 对象存储
        Date dateToSend = DateUtil.parse(formattedDate, "MMM dd, yyyy hh:mm:ss a");

        // 设置到 AIMsg 对象中
//        AIMsg.setSendTime(dateToSend);

        // 验证存储的 Date 对象
        System.out.println("Stored Date: " + DateUtil.format(dateToSend, "yyyy-MM-dd HH:mm:ss"));
        return dateToSend;
    }

    @GetMapping("/test2")
    public Flux<String> GetHello2(@RequestParam(defaultValue = "你是谁") String message, @RequestParam(defaultValue = "1") Long memoryId) {

        return Flux.create(sink -> {
            assistantUnique.stream(String.valueOf(memoryId), message, "你是一个AI助手").onPartialResponse(partialResponse -> {
                System.out.println("partialResponse:" + partialResponse);
                sink.next(partialResponse);
            }).onCompleteResponse(partialResponse -> {
                System.out.println("完成:" + partialResponse);
            }).onError(partialResponse -> {
                System.out.println("出错:" + partialResponse);
            });
        });
    }

    @GetMapping("/test3")
    private void test3() throws IOException {

    }
}
