package com.myAI.myAI.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myAI.myAI.constant.OperationType;
import com.myAI.myAI.models.entity.OperationLog;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志服务
 */
public interface OperationLogService extends IService<OperationLog> {
    
    /**
     * 记录操作日志
     *
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param description 操作描述
     * @param success 是否成功
     * @param businessId 业务ID
     * @param request HTTP请求
     */
    void recordOperationLog(Long userId, 
                          OperationType operationType, 
                          String description, 
                          Boolean success, 
                          String businessId, 
                          HttpServletRequest request);

    /**
     * 异步记录操作日志
     */
    void asyncRecordOperationLog(Long userId, 
                               OperationType operationType, 
                               String description, 
                               Boolean success, 
                               String businessId, 
                               HttpServletRequest request);
} 