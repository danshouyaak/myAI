package com.myAI.myAI.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myAI.myAI.models.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
} 