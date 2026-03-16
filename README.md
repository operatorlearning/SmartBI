# SmartBI — 智能BI数据分析平台

> AI 驱动的数据可视化与智能分析系统，面向简历展示的 Java 后端项目

## 项目定位

| | MallOrder (项目一) | **SmartBI (项目二)** |
|---|---|---|
| **领域** | 高并发电商订单/秒杀 | AI 数据分析/可视化 |
| **核心亮点** | Redis Lua预减库存、MQ延迟关单 | **AI Prompt工程、异步任务编排、策略模式** |
| **数据处理** | 库存并发扣减 | **EasyExcel SAX流式解析百万级数据** |
| **实时通信** | — | **SSE 进度推送** |
| **消息队列** | 延迟队列+死信关单 | **异步AI任务+死信兜底** |

---

## 技术栈

| 层级 | 技术 |
|---|---|
| 框架 | Spring Boot 3.2 + Java 17 |
| 安全 | Spring Security + JWT 认证 |
| ORM | MyBatis-Plus 3.5（分页插件 + 字段自动填充） |
| 缓存 | Redis（限流计数器、缓存） |
| 消息 | RabbitMQ（异步任务队列 + 死信交换机） |
| AI | OpenAI 兼容 API（DeepSeek / ChatGLM / GPT） |
| Excel | EasyExcel 3.3（SAX 流式解析） |
| 数据库 | MySQL 8.0 + Druid 连接池 |
| 前端 | Vue 3 + Element Plus + ECharts（CDN SPA） |
| 文档 | Knife4j（Swagger 增强） |

---

## 5 大功能模块

### 1. 用户模块
- 注册/登录（BCrypt 加密）
- JWT Token 认证
- RBAC 角色权限

### 2. 数据集模块
- **EasyExcel SAX 模式**流式解析（内存占用极低）
- 自动推断列类型（数字/文本）
- 统一转换 CSV 格式供 AI 分析
- 数据集 CRUD 管理

### 3. 智能图表模块（核心）
- **同步模式**: 上传数据 → AI 分析 → 实时返回 ECharts 图表
- **异步模式**: 提交任务 → MQ 异步处理 → SSE 实时推送进度
- 支持 5 种图表类型：柱状图/折线图/饼图/雷达图/散点图
- AI 自动选择最佳图表类型

### 4. AI 服务模块
- **Prompt 工程**: 精心设计 System Prompt 约束 AI 输出结构化 JSON
- **策略模式**: 支持真实 API 调用和 Mock 模式无缝切换
- 兼容 OpenAI / DeepSeek / ChatGLM 等多种 AI API
- 超时控制 + 响应解析 + 错误兜底

### 5. 运维功能
- `@OperLog` AOP 操作日志
- `@RateLimit` Redis Lua 滑动窗口限流
- 全局异常处理
- Knife4j API 文档

---

## 核心技术亮点

### AI 智能分析流程
```
用户上传Excel → EasyExcel SAX解析 → CSV格式化
    → Prompt工程组装 → AI API调用 → 响应解析
    → ECharts Option生成 → 前端可视化渲染
```

### 异步任务架构
```
提交分析 → 创建Chart(wait) → 创建AiTask(pending)
    → MQ发送消息 → Consumer消费
    → 更新进度(10%→30%→70%→100%)
    → SSE实时推送 → 前端EventSource接收
    → 失败进入死信队列兜底
```

### 接口限流（Redis Lua 滑动窗口）
```
@RateLimit注解 → AOP拦截 → Redis ZADD时间戳
    → ZREMRANGEBYSCORE清理过期 → ZCARD计数
    → 超限拒绝 → Lua脚本保证原子性
```

### EasyExcel 流式解析
```
SAX模式逐行读取 → 无需加载全部数据到内存
    → 自动推断列类型 → 转CSV格式
    → 支持百万行Excel文件
```

---

## 项目规模

- **后端**: ~45 个 Java 文件
  - 4 Controller（User、Dataset、Chart、AiTask）
  - 3 Service + 3 Impl
  - 5 Mapper
  - 5 Entity
  - 4 DTO + 4 VO
  - 7 Config
  - 3 Security
  - 2 Annotation + 2 Aspect
  - 2 Util
  - 1 MQ Producer + 1 MQ Consumer
  - 1 GlobalExceptionHandler
- **前端**: Vue 3 SPA 单页（首页、数据集、AI分析、图表列表/详情、登录/注册）
- **数据库**: 5 张表（用户、数据集、图表、AI任务、操作日志）

---

## 数据库设计

| 表名 | 说明 | 核心字段 |
|---|---|---|
| user | 用户表 | username, password(BCrypt), role |
| dataset | 数据集表 | raw_data(CSV), columns_info(JSON), row_count |
| chart | 图表表 | chart_option(ECharts JSON), analysis_result, status |
| ai_task | AI任务表 | status, progress(0-100), start_time, end_time |
| operation_log | 操作日志表 | operation, method, duration, ip |

---

## 快速启动

### 1. 启动基础设施（Docker）

```bash
docker-compose up -d
```

启动 MySQL(3306)、Redis(6379)、RabbitMQ(5672/15672)

### 2. 启动后端

```bash
mvn spring-boot:run
```

### 3. 访问系统

- 前端地址: http://localhost:8089/api/index.html
- API 文档: http://localhost:8089/api/doc.html
- RabbitMQ 管理: http://localhost:15672 (guest/guest)
- 演示账号: admin / admin123

### 4. AI 配置（可选）

默认启用 Mock 模式，配置真实 API Key 后自动切换：

```yaml
smartbi:
  ai:
    api-key: sk-your-api-key  # DeepSeek / OpenAI Key
    mock-enabled: false
```

---

## 项目结构

```
src/main/java/com/smartbi/
├── SmartBiApplication.java          # 启动类
├── common/                          # 通用类
│   ├── Result.java                  # 统一响应
│   ├── ErrorCode.java               # 错误码枚举
│   ├── BusinessException.java       # 业务异常
│   └── PageResult.java              # 分页结果
├── config/                          # 配置类
│   ├── SecurityConfig.java          # Spring Security
│   ├── MyBatisPlusConfig.java       # 分页+自动填充
│   ├── RedisConfig.java             # Redis序列化
│   ├── RabbitMQConfig.java          # MQ交换机/队列
│   ├── ThreadPoolConfig.java        # AI任务线程池
│   ├── WebMvcConfig.java            # CORS+静态资源
│   └── Knife4jConfig.java           # API文档
├── security/                        # 安全认证
│   ├── JwtTokenProvider.java        # JWT工具
│   ├── JwtAuthenticationFilter.java # JWT过滤器
│   └── UserDetailsServiceImpl.java  # 用户认证
├── entity/                          # 实体类
├── mapper/                          # MyBatis Mapper
├── dto/                             # 请求DTO
├── vo/                              # 响应VO
├── service/                         # 服务接口
│   └── impl/                        # 服务实现
├── controller/                      # 控制器
├── mq/                              # 消息队列
│   ├── AiMessageProducer.java       # 任务生产者
│   └── AiMessageConsumer.java       # 任务消费者
├── annotation/                      # 自定义注解
│   ├── RateLimit.java               # 限流注解
│   └── OperLog.java                 # 操作日志注解
├── aspect/                          # AOP切面
│   ├── RateLimitAspect.java         # 限流(Lua滑动窗口)
│   └── OperLogAspect.java           # 操作日志
├── handler/                         # 异常处理
│   └── GlobalExceptionHandler.java
└── util/                            # 工具类
    ├── ExcelUtils.java              # EasyExcel解析
    └── AiPromptBuilder.java         # Prompt工程
```

---


