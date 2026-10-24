# 家政上门服务派单系统

一个覆盖「下单 → 派单 → 履约 → 结算」全链路的家政服务平台，重点解决时间窗冲突、一单多派、超时流转、履约可信与结算对账。

## 技术栈

| 层 | 选型 |
|---|---|
| 语言 | Java 17 |
| 后端 | Spring Boot 3.3、MyBatis-Plus |
| 存储 | MySQL 8、Redis 7 |
| 消息 | RabbitMQ（延迟 / 重试）、Kafka（订单事件流，骨架阶段未启用） |
| 前端 | Vue 3 + Element Plus（AI 辅助） |
| 部署 | Nginx、Docker Compose |

## 快速开始

### 1. 建库

```bash
mysql -uroot -p < src/main/resources/db/schema.sql
```

### 2. 配置

`application.yml` 默认连接本机，可用环境变量覆盖：

```
MYSQL_HOST / MYSQL_PORT / MYSQL_DB / MYSQL_USER / MYSQL_PASSWORD
REDIS_HOST / REDIS_PORT / REDIS_PASSWORD
RABBITMQ_HOST / RABBITMQ_PORT / RABBITMQ_USER / RABBITMQ_PASSWORD
KAFKA_SERVERS / SERVER_PORT
JWT_SECRET / SMS_PROVIDER
```

### 3. 启动

```bash
mvn spring-boot:run
```

访问 `http://localhost:8080/api/ping` 验证。

### 4. 测试

```bash
mvn test
```

## 工程结构

```
src/main/java/com/homeservice
├── common      通用返回、异常、TraceId、MyBatis-Plus / CORS 配置
├── auth        验证码登录、JWT、短信通道抽象
├── user        用户与角色
├── order       订单实体、状态机、Mapper
├── dispatch    派单任务、时间槽、槽位占用服务
├── worker      服务人员
└── catalog     服务目录
```

## 登录鉴权

手机号 + 验证码，验证码存 Redis，登录成功签发 JWT。

```
POST /api/auth/sms/send    { "phone": "13800138000" }
POST /api/auth/login/sms   { "phone": "...", "code": "123456", "role": "CUSTOMER" }
GET  /api/auth/me          Authorization: Bearer <token>
POST /api/auth/logout
```

开发环境默认 `sms.provider=mock`，验证码直接打在应用日志里：

```
[MockSms] 登录验证码 phone=13800138000, code=546287
```

- 验证码 5 分钟有效、60 秒冷却、最多错 5 次
- 客户端首次登录自动注册；服务人员账号需先入驻审核
- JWT 默认 2 小时，退出登录把 `jti` 加入 Redis 黑名单
- 生产环境必须改 `JWT_SECRET`，并把 `SMS_PROVIDER` 换成真实通道

## 文档

- 需求与设计文档：`../家政派单系统-需求与设计.md`（仓库外，不纳入版本控制）

## 当前进度

- [x] 需求与领域设计
- [x] 工程骨架
- [x] 手机号验证码登录（JWT）
- [ ] 下单与状态机
- [ ] 派单引擎
- [ ] 履约与结算
