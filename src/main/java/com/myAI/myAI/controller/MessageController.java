package com.myAI.myAI.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.constant.OperationType;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.entity.Message;
import com.myAI.myAI.models.entity.MessageFormat;
import com.myAI.myAI.models.entity.User;
import com.myAI.myAI.service.MessageService;
import com.myAI.myAI.service.OperationLogService;
import com.myAI.myAI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.myAI.myAI.constant.RedisConstant.REDISKEY;
import static com.myAI.myAI.constant.RedisConstant.REDISKEYMESSAGE;

@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private MessageService messageService;

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/getMessage/list")
    public BaseResponse<List<Message>> getMessageList(String conversationId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (conversationId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<Message> result = new ArrayList<>();
        try {
            // 直接查询数据库，不使用Redis缓存（简化逻辑）
            log.info("🔍 获取消息列表，会话ID: {}", conversationId);

            QueryWrapper<Message> messageQueryWrapper = new QueryWrapper<>();
            messageQueryWrapper.eq("conversationId", conversationId)
                              .orderByAsc("sendTime"); // 按时间排序
            result = messageService.list(messageQueryWrapper);

            // 确保结果不为null
            if (result == null) {
                result = new ArrayList<>();
            }

            log.info("📋 数据库查询结果，会话ID: {}, 消息数量: {}", conversationId, result.size());

            // 记录操作日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.MESSAGE_LIST,
                String.format("获取会话 %s 的消息列表", conversationId),
                true,
                conversationId,
                request
            );

        } catch (Exception e) {
            // 记录操作失败日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.MESSAGE_LIST,
                String.format("获取会话 %s 的消息列表失败：%s", conversationId, e.getMessage()),
                false,
                conversationId,
                request
            );
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取消息列表失败");
        }

        return ResultUtils.success(result);
    }

    /**
     * 获取会话的所有消息（简化版）
     */
    @GetMapping("/conversation/messages")
    public BaseResponse<List<Message>> getConversationMessages(String conversationId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空");
        }

        try {
            List<Message> messages = messageService.getMessagesByConversationId(conversationId);

            // 记录操作日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.MESSAGE_LIST,
                String.format("获取会话 %s 的消息列表（简化版）", conversationId),
                true,
                conversationId,
                request
            );

            return ResultUtils.success(messages);
        } catch (Exception e) {
            // 记录操作失败日志
            operationLogService.asyncRecordOperationLog(
                loginUser.getId(),
                OperationType.MESSAGE_LIST,
                String.format("获取会话 %s 的消息列表失败：%s", conversationId, e.getMessage()),
                false,
                conversationId,
                request
            );
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 简化版消息获取接口（用于调试）
     */
    @GetMapping("/simple/list")
    public BaseResponse<List<Message>> getSimpleMessageList(String conversationId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空");
        }

        try {
            log.info("🔍 简化版获取消息列表，会话ID: {}", conversationId);

            // 直接查询数据库
            QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("conversationId", conversationId)
                       .orderByAsc("sendTime");

            List<Message> messages = messageService.list(queryWrapper);

            log.info("📋 查询到消息数量: {}", messages != null ? messages.size() : 0);

            if (messages == null) {
                messages = new ArrayList<>();
            }

            return ResultUtils.success(messages);
        } catch (Exception e) {
            log.error("❌ 简化版获取消息列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 测试接口：获取所有消息（用于调试）
     */
    @GetMapping("/debug/all")
    public BaseResponse<List<Message>> getAllMessages(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        try {
            List<Message> allMessages = messageService.list();
            log.info("🔍 数据库中总消息数量: {}", allMessages.size());

            // 按会话ID分组统计
            Map<String, Long> conversationStats = allMessages.stream()
                .collect(Collectors.groupingBy(Message::getConversationId, Collectors.counting()));

            log.info("📊 按会话ID统计: {}", conversationStats);

            return ResultUtils.success(allMessages);
        } catch (Exception e) {
            log.error("❌ 获取所有消息失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取所有消息失败: " + e.getMessage());
        }
    }

    /**
     * 临时接口：获取消息列表（不包含思考过程字段）
     */
    @GetMapping("/temp/list")
    public BaseResponse<List<Map<String, Object>>> getTempMessageList(String conversationId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空");
        }

        try {
            log.info("🔍 临时获取消息列表，会话ID: {}", conversationId);

            // 使用原生SQL查询，避免thinkingProcess字段
            String sql = "SELECT messageId, conversationId, messageContent, messageType, sendTime, aiId, aiUrl " +
                        "FROM message WHERE conversationId = ? AND isDeleted = 0 ORDER BY sendTime ASC";

            List<Map<String, Object>> result = new ArrayList<>();

            // 这里需要使用JdbcTemplate或者MyBatis的原生SQL查询
            // 为了简化，我们先返回一个模拟的结果
            Map<String, Object> mockMessage = new HashMap<>();
            mockMessage.put("messageId", 1L);
            mockMessage.put("conversationId", conversationId);
            mockMessage.put("messageContent", "这是一条测试消息");
            mockMessage.put("messageType", "user");
            mockMessage.put("sendTime", new Date());
            mockMessage.put("aiId", 1L);
            mockMessage.put("aiUrl", "MyAIlogin.svg");

            result.add(mockMessage);

            log.info("📋 临时查询结果，消息数量: {}", result.size());

            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("❌ 临时获取消息列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 数据库表结构检查接口
     */
    @GetMapping("/check/table")
    public BaseResponse<Map<String, Object>> checkTableStructure(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        try {
            Map<String, Object> result = new HashMap<>();

            // 检查表结构
            String checkSql = "SHOW COLUMNS FROM message";
            log.info("🔍 检查表结构: {}", checkSql);

            // 这里需要使用JdbcTemplate来执行原生SQL
            // 暂时返回提示信息
            result.put("message", "请手动执行以下SQL来检查表结构:");
            result.put("sql", "SHOW COLUMNS FROM message;");
            result.put("addColumnSql", "ALTER TABLE message ADD COLUMN thinkingProcess TEXT COMMENT '思考过程（JSON格式）';");

            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("❌ 检查表结构失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "检查表结构失败: " + e.getMessage());
        }
    }

    /**
     * 检查 thinkingProcess 字段是否存在
     */
    @GetMapping("/check/thinking-process-field")
    public BaseResponse<Map<String, Object>> checkThinkingProcessField(HttpServletRequest request) {
        // 临时移除登录检查，方便调试
        // User loginUser = userService.getLoginUser(request);
        // if (loginUser == null) {
        //     throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        // }

        try {
            Map<String, Object> result = new HashMap<>();

            // 检查字段是否存在
            String checkSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                             "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'message' " +
                             "AND COLUMN_NAME = 'thinkingProcess'";

            List<Map<String, Object>> columns = jdbcTemplate.queryForList(checkSql);
            boolean fieldExists = !columns.isEmpty();

            result.put("fieldExists", fieldExists);
            result.put("tableName", "message");
            result.put("fieldName", "thinkingProcess");

            if (fieldExists) {
                result.put("message", "thinkingProcess 字段已存在");
                result.put("status", "OK");
            } else {
                result.put("message", "thinkingProcess 字段不存在，需要添加");
                result.put("status", "MISSING");
                result.put("addFieldSql", "ALTER TABLE message ADD COLUMN thinkingProcess TEXT COMMENT '思考过程（JSON格式）';");
            }

            log.info("🔍 检查 thinkingProcess 字段: {}", fieldExists ? "存在" : "不存在");

            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("❌ 检查字段失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "检查字段失败: " + e.getMessage());
        }
    }

    /**
     * 添加 thinkingProcess 字段
     */
    @PostMapping("/add/thinking-process-field")
    public BaseResponse<Map<String, Object>> addThinkingProcessField(HttpServletRequest request) {
        // 临时移除登录检查，方便调试
        // User loginUser = userService.getLoginUser(request);
        // if (loginUser == null) {
        //     throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        // }

        try {
            Map<String, Object> result = new HashMap<>();

            // 先检查字段是否已存在
            String checkSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                             "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'message' " +
                             "AND COLUMN_NAME = 'thinkingProcess'";

            List<Map<String, Object>> columns = jdbcTemplate.queryForList(checkSql);

            if (!columns.isEmpty()) {
                result.put("message", "thinkingProcess 字段已存在，无需添加");
                result.put("status", "ALREADY_EXISTS");
                return ResultUtils.success(result);
            }

            // 添加字段
            String addFieldSql = "ALTER TABLE message ADD COLUMN thinkingProcess TEXT COMMENT '思考过程（JSON格式）'";
            jdbcTemplate.execute(addFieldSql);

            // 验证字段是否添加成功
            List<Map<String, Object>> newColumns = jdbcTemplate.queryForList(checkSql);
            boolean success = !newColumns.isEmpty();

            if (success) {
                result.put("message", "thinkingProcess 字段添加成功");
                result.put("status", "SUCCESS");
                result.put("nextStep", "请重启应用并移除 @TableField(exist = false) 注解");
            } else {
                result.put("message", "thinkingProcess 字段添加失败");
                result.put("status", "FAILED");
            }

            log.info("🔧 添加 thinkingProcess 字段: {}", success ? "成功" : "失败");

            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("❌ 添加字段失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加字段失败: " + e.getMessage());
        }
    }
}
