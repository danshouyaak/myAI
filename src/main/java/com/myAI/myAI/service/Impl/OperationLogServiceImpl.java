package com.myAI.myAI.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.myAI.myAI.constant.OperationType;
import com.myAI.myAI.mapper.OperationLogMapper;
import com.myAI.myAI.models.entity.OperationLog;
import com.myAI.myAI.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * 操作日志服务实现类
 */
@Service
@Slf4j
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Resource
    private Gson gson;

    @Override
    public void recordOperationLog(Long userId,
                                 OperationType operationType,
                                 String description,
                                 Boolean success,
                                 String businessId,
                                 HttpServletRequest request) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setOperationType(operationType.name());
            operationLog.setDescription(description);
            operationLog.setSuccess(success);
            operationLog.setBusinessId(businessId);
            
            // 设置IP地址
            String ipAddress = request.getRemoteAddr();
            operationLog.setIpAddress(ipAddress);
            
            // 设置请求参数
            Map<String, String[]> parameterMap = request.getParameterMap();
            operationLog.setRequestParams(gson.toJson(parameterMap));
            
            // 设置时间
            Date now = new Date();
            operationLog.setOperationTime(now);
            operationLog.setCreateTime(now);
            operationLog.setUpdateTime(now);
            
            // 保存日志
            this.save(operationLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    @Async
    @Override
    public void asyncRecordOperationLog(Long userId,
                                      OperationType operationType,
                                      String description,
                                      Boolean success,
                                      String businessId,
                                      HttpServletRequest request) {
        recordOperationLog(userId, operationType, description, success, businessId, request);
    }
} 