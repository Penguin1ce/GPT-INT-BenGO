# RAG Demo - GPT-INT 后端API

基于Spring Boot 3.5实现的RAG (Retrieval-Augmented Generation) 系统后端API，严格按照GPT-INT后端API接口规范实现。

## 功能特性

### 🔐 认证系统
- 用户注册与登录
- JWT令牌认证
- 刷新令牌机制
- 用户登出
- 用户信息获取

### 💬 GPT对话
- 支持GPT-4o等多种模型
- 流式和非流式响应
- 多轮对话支持
- 令牌使用统计

### 📁 文件管理
- 文件上传（支持txt、md、pdf、docx）
- 文件列表查询
- 分页支持
- 文件大小限制（10MB）

### 🔧 技术特性
- 统一响应格式
- 全局异常处理
- 参数验证
- 数据库事务管理
- 安全配置

## 技术栈

- **Spring Boot 3.5.5** - 主框架
- **Spring Security** - 安全认证
- **Spring Data JPA** - 数据库操作
- **PostgreSQL** - 主数据库
- **Redis** - 缓存
- **JWT** - 令牌认证
- **Spring AI** - OpenAI集成
- **Lombok** - 代码简化

## 快速开始

### 环境要求
- Java 21
- Maven 3.6+
- PostgreSQL 12+
- Redis 6.0+

### 数据库准备
```bash
# 创建PostgreSQL数据库
createdb ragdemo
```

### 配置文件
编辑 `src/main/resources/application.yaml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ragdemo
    username: your-db-username
    password: your-db-password
  
  data:
    redis:
      host: localhost
      port: 6379
  
  ai:
    openai:
      api-key: your-openai-api-key
      base-url: https://api.openai.com

app:
  jwt:
    secret: your-secret-key
```

### 运行项目
```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

项目将在 `http://localhost:8000` 启动。

## API接口

### 认证相关
- `POST /auth/register` - 用户注册
- `POST /auth/login` - 用户登录
- `POST /auth/refresh` - 刷新令牌
- `POST /auth/logout` - 用户登出
- `GET /auth/profile` - 获取用户信息

### 对话相关
- `POST /ask` - GPT对话（支持流式响应）

### 文件相关
- `POST /upload` - 文件上传
- `GET /files` - 获取文件列表

## 统一响应格式

所有API响应都遵循以下格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {},
  "code": 200
}
```

错误响应：
```json
{
  "success": false,
  "message": "错误信息",
  "code": 400,
  "errors": [
    {
      "field": "字段名",
      "message": "错误详情"
    }
  ]
}
```

## 项目结构

```
src/main/java/com/firefly/ragdemo/
├── config/          # 配置类
├── controller/      # 控制器
├── DTO/            # 请求数据传输对象
├── entity/         # 实体类
├── exception/      # 异常处理
├── mapper/         # Repository接口
├── secutiry/       # 安全相关
├── service/        # 业务服务
├── VO/             # 响应数据传输对象
└── RaGdemoApplication.java
```

## 数据库表结构

### users - 用户表
- id (VARCHAR(36), PK)
- username (VARCHAR(50), UNIQUE)
- email (VARCHAR(100), UNIQUE)
- password_hash (VARCHAR(255))
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
- is_active (BOOLEAN)
- last_login (TIMESTAMP)

### refresh_tokens - 刷新令牌表
- id (VARCHAR(36), PK)
- user_id (VARCHAR(36), FK)
- token (VARCHAR(500))
- expires_at (TIMESTAMP)
- created_at (TIMESTAMP)
- is_revoked (BOOLEAN)

### uploaded_files - 上传文件表
- id (VARCHAR(36), PK)
- user_id (VARCHAR(36), FK)
- filename (VARCHAR(255))
- file_path (VARCHAR(500))
- file_size (BIGINT)
- file_type (VARCHAR(50))
- upload_time (TIMESTAMP)
- status (ENUM: 'PROCESSING', 'COMPLETED', 'FAILED')

## 安全特性

- 密码使用BCrypt加密
- JWT令牌认证
- CORS配置
- 请求参数验证
- 文件类型和大小限制
- SQL注入防护

## 开发说明

本项目严格按照 `GPT-INT后端API接口规范.md` 实现，包含：

1. ✅ 完整的用户认证系统
2. ✅ JWT令牌机制
3. ✅ GPT对话接口（流式/非流式）
4. ✅ 文件上传管理
5. ✅ 统一错误处理
6. ✅ 参数验证
7. ✅ 数据库设计

## 许可证

MIT License

## 环境变量

在运行前，请通过环境变量提供敏感配置（不要把密钥写入仓库）：

- `OPENAI_API_KEY`：OpenAI API密钥（必填）
- `OPENAI_BASE_URL`：OpenAI Base URL（可选，默认 https://api.csun.site）
- `APP_JWT_SECRET`：JWT签名密钥（建议设置为强随机串）

常见用法：

```bash
# Windows PowerShell
$env:OPENAI_API_KEY="sk-xxxx"; $env:OPENAI_BASE_URL="https://api.openai.com"; $env:APP_JWT_SECRET="your-strong-secret"; mvn spring-boot:run

# Linux / macOS
OPENAI_API_KEY=sk-xxxx OPENAI_BASE_URL=https://api.openai.com APP_JWT_SECRET=your-strong-secret mvn spring-boot:run
```

也可在本地创建`.env`文件（已被.gitignore忽略），并参考`.env.example`：

```env
OPENAI_API_KEY=sk-xxxx
OPENAI_BASE_URL=https://api.openai.com
APP_JWT_SECRET=your-strong-secret
```
