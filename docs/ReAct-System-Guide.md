# ReAct 系统使用指南

## 概述

ReAct (Reasoning and Acting) 是一个智能推理和行动系统，它通过系统性的思考、行动和观察来解决复杂问题。本系统集成了多种工具，支持记忆管理和错误恢复，能够处理各种类型的任务。

## 系统架构

### 核心组件

1. **ReactProcessor** - ReAct核心处理器
   - 实现思考→行动→观察的循环
   - 支持流式响应(SSE)
   - 智能错误处理和重试机制

2. **ReactToolManager** - 工具管理器
   - 管理所有可用工具
   - 动态工具注册和发现
   - 工具可用性检查

3. **ReactMemoryManager** - 记忆管理器
   - 持久化思考过程
   - 会话历史管理
   - 上下文信息存储

4. **ReactErrorHandler** - 错误处理器
   - 智能错误分类
   - 自动恢复策略
   - 重试机制

### 可用工具

1. **WEB_SEARCH** - 网络搜索
   - 支持多搜索引擎
   - 实时信息获取
   - 结果格式化

2. **CALCULATOR** - 计算器
   - 基本数学运算
   - 三角函数
   - 对数函数

3. **CODE_EXECUTOR** - 代码执行器
   - JavaScript代码执行
   - 安全沙箱环境
   - 超时保护

4. **TEXT_PROCESSOR** - 文本处理器
   - 文本统计分析
   - 信息提取
   - 格式化处理

5. **DATETIME** - 时间日期工具
   - 日期计算
   - 时区转换
   - 格式化

## API 接口

### 1. 流式处理接口

```http
GET /react/process/stream?input={用户输入}&sessionId={会话ID}
```

**参数:**
- `input` (必需): 用户输入的问题或任务
- `sessionId` (可选): 会话ID，用于记忆管理

**响应:** Server-Sent Events (SSE) 流

**事件类型:**
- `start`: 处理开始
- `session`: 会话ID
- `memory`: 记忆加载状态
- `progress`: 处理进度
- `message`: 思考结果
- `error`: 错误信息

### 2. 记忆管理接口

#### 获取会话历史
```http
GET /react/memory/history?sessionId={会话ID}
```

#### 获取会话统计
```http
GET /react/memory/stats?sessionId={会话ID}
```

#### 清理会话数据
```http
DELETE /react/memory/clear?sessionId={会话ID}
```

## 使用示例

### 1. 基本计算任务

```javascript
// 前端JavaScript示例
const eventSource = new EventSource('/react/process/stream?input=计算 2 + 3 * 4 的结果');

eventSource.onmessage = function(event) {
    const data = JSON.parse(event.data);
    console.log('思考过程:', data);
};

eventSource.addEventListener('error', function(event) {
    console.error('处理错误:', event.data);
});
```

### 2. 复杂搜索任务

```http
GET /react/process/stream?input=搜索最新的AI技术发展并总结主要趋势&sessionId=user_123
```

### 3. 文本处理任务

```http
GET /react/process/stream?input=统计这段文本的字数和词频：人工智能正在改变世界
```

## 工作流程

### ReAct循环

1. **思考阶段 (Thinking)**
   - 分析用户问题
   - 制定解决策略
   - 选择合适工具

2. **行动阶段 (Acting)**
   - 执行选定工具
   - 传递正确参数
   - 处理执行结果

3. **观察阶段 (Observing)**
   - 分析工具输出
   - 评估结果质量
   - 决定下一步行动

4. **评估阶段 (Evaluation)**
   - 判断是否达成目标
   - 决定继续或结束
   - 生成最终答案

### 错误处理流程

1. **错误检测**
   - 自动分类错误类型
   - 记录错误上下文

2. **恢复策略**
   - 根据错误类型选择策略
   - 提供具体恢复建议

3. **重试机制**
   - 智能重试判断
   - 指数退避延迟

## 配置说明

### 系统参数

```yaml
# application.yml
react:
  max-iterations: 5        # 最大思考轮数
  max-tool-retries: 3      # 工具最大重试次数
  response-timeout: 30     # 响应超时时间(秒)
  memory-ttl-days: 7       # 记忆保存天数
  session-ttl-hours: 24    # 会话保存小时数
```

### Redis配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 2000ms
```

## 最佳实践

### 1. 问题描述

- **明确具体**: 提供清晰的问题描述
- **包含上下文**: 提供必要的背景信息
- **分步骤**: 复杂任务可以分解为多个步骤

### 2. 会话管理

- **使用会话ID**: 为相关对话使用相同的会话ID
- **定期清理**: 及时清理不需要的会话数据
- **监控使用**: 关注记忆使用情况

### 3. 错误处理

- **监听错误事件**: 及时处理错误响应
- **重试策略**: 对临时错误进行适当重试
- **降级方案**: 准备备用处理方案

## 监控和调试

### 日志级别

```yaml
logging:
  level:
    com.myAI.myAI.ai.react: DEBUG
```

### 关键指标

- 处理成功率
- 平均处理时间
- 工具使用频率
- 错误类型分布
- 记忆命中率

### 调试技巧

1. **查看详细日志**: 启用DEBUG级别日志
2. **监控SSE流**: 观察完整的思考过程
3. **检查工具状态**: 确认工具可用性
4. **验证记忆数据**: 检查Redis中的数据

## 扩展开发

### 添加新工具

1. 实现 `ReactTool` 接口
2. 在 `ReactToolManager` 中注册
3. 添加相应的测试用例

```java
@Component
public class CustomTool implements ReactTool {
    @Override
    public String getName() {
        return "CUSTOM_TOOL";
    }
    
    @Override
    public String getDescription() {
        return "自定义工具描述";
    }
    
    @Override
    public String execute(String input) {
        // 工具实现逻辑
        return "执行结果";
    }
    
    // 其他必需方法...
}
```

### 自定义错误处理

```java
@Component
public class CustomErrorHandler extends ReactErrorHandler {
    // 重写错误处理逻辑
}
```

## 故障排除

### 常见问题

1. **工具执行失败**
   - 检查工具依赖服务
   - 验证参数格式
   - 查看错误日志

2. **记忆加载失败**
   - 检查Redis连接
   - 验证会话ID格式
   - 确认数据完整性

3. **SSE连接中断**
   - 检查网络连接
   - 调整超时设置
   - 实现重连机制

### 性能优化

1. **工具缓存**: 缓存工具执行结果
2. **记忆压缩**: 压缩存储的记忆数据
3. **连接池**: 优化Redis连接池配置
4. **异步处理**: 使用异步方式处理长时间任务

## 版本更新

### v1.0.0 (当前版本)
- 基础ReAct框架
- 5个核心工具
- 记忆管理系统
- 错误处理机制

### 计划功能
- 更多专业工具
- 多模态支持
- 分布式部署
- 性能监控面板
