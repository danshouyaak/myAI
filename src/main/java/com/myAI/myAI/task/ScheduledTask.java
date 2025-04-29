//package com.myAI.myAI.task;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateTime;
//import cn.hutool.core.date.DateUtil;
//import com.google.gson.Gson;
//import com.myAI.myAI.common.ErrorCode;
//import com.myAI.myAI.exception.BusinessException;
//import com.myAI.myAI.models.entity.Message;
//import com.myAI.myAI.models.entity.MessageFormat;
//import com.myAI.myAI.service.MessageService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static com.myAI.myAI.constant.RedisConstant.REDISKEY;
//
//@Slf4j
//@Component
//public class ScheduledTask {
//
//    // 定义目标格式
//    static String targetFormat = "MMM dd, yyyy hh:mm:ss a";
//    @Resource
//    private MessageService messageService;
//    @Resource
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Scheduled(cron = "0 0 */1 * * ?")  // 每小时执行一次
////    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
//    public void task() {
//        log.info("定时任务执行了 把数据库更新到redis上，当前时间：{}", new Date());
//        List<Message> list = messageService.list();
//
//        if (CollUtil.isEmpty(list)) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "数据为空");
//        }
//        Gson gson = new Gson();
//        List<String> collect = list.stream().map(g -> {
//            MessageFormat messageFormat = new MessageFormat();
//            BeanUtils.copyProperties(g, messageFormat);
//            Date sendTime = g.getSendTime();
//            String formatTime = DateUtil.format(sendTime, "yyyy-MM-dd HH:mm:ss");
//            messageFormat.setSendTime(formatTime);
//            return gson.toJson(messageFormat);
//        }).collect(Collectors.toList());
//
//        // 使用管道（Pipeline）来提高效率
//        redisTemplate.delete(REDISKEY);
//        // 插入新数据
//        redisTemplate.opsForList().rightPushAll(REDISKEY, collect);
//
//        log.info("定时任务结束了 redis已经是最新状态，当前时间：{}", new Date());
//    }
//}
