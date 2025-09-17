# GPT-INT 后端API接口规范

## 目录
1. [概述](#概述)
2. [数据库设计](#数据库设计) 
3. [认证相关接口](#认证相关接口)
4. [业务功能接口](#业务功能接口)
5. [错误处理规范](#错误处理规范)
6. [JWT Token处理](#jwt-token处理)
7. [实现示例](#实现示例)

---

## 概述

### 基础URL
```
http://localhost:8000
```

### 请求头格式
```http
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>  # 需要认证的接口
```

### 统一响应格式
```json
{
  "success": true,
  "message": "操作成功",
  "data": {}, // 具体数据
  "code": 200 // 状态码
}
```

---

## 数据库设计

### 用户表 (users)
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL
);
```

### 刷新Token表 (refresh_tokens)
```sql
CREATE TABLE refresh_tokens (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36) NOT NULL,
    token VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 文件上传记录表 (uploaded_files)
```sql
CREATE TABLE uploaded_files (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('processing', 'completed', 'failed') DEFAULT 'processing',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 认证相关接口

### 1. 用户注册

**接口地址**：`POST /auth/register`

**请求参数**：
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**参数验证**：
- username: 3-20个字符，只允许字母、数字、下划线
- email: 有效的邮箱格式
- password: 6-20个字符

**响应示例**：
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "testuser",
      "email": "test@example.com",
      "created_at": "2024-01-01T00:00:00Z"
    }
  },
  "code": 201
}
```

**错误响应**：
```json
{
  "success": false,
  "message": "用户名已存在",
  "code": 409
}
```

### 2. 用户登录

**接口地址**：`POST /auth/login`

**请求参数**：
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**响应示例**：
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "testuser",
      "email": "test@example.com"
    }
  },
  "code": 200
}
```

**JWT Token Payload**：
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "testuser",
  "iat": 1640995200,
  "exp": 1641081600
}
```

### 3. 刷新Token

**接口地址**：`POST /auth/refresh`

**请求参数**：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应示例**：
```json
{
  "success": true,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "code": 200
}
```

### 4. 用户登出

**接口地址**：`POST /auth/logout`

**请求头**：
```http
Authorization: Bearer <JWT_TOKEN>
```

**请求参数**：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // 可选
}
```

**响应示例**：
```json
{
  "success": true,
  "message": "登出成功",
  "code": 200
}
```

---

## 业务功能接口

### 1. GPT对话接口

**接口地址**：`POST /ask`

**请求头**：
```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**请求参数**：
```json
{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "user",
      "content": "请解释这段代码的功能"
    },
    {
      "role": "assistant", 
      "content": "这段代码的主要功能是..."
    },
    {
      "role": "user",
      "content": "const arr = [1,2,3]; console.log(arr);"
    }
  ],
  "stream": true,
  "langid": "js"
}
```

**流式响应格式**：
```
data: {"message": {"content": "这段"}}

data: {"message": {"content": "代码"}}

data: {"message": {"content": "是用来"}}

...
```

**非流式响应格式**：
```json
{
  "success": true,
  "message": "对话完成",
  "data": {
    "response": "这段JavaScript代码创建了一个包含数字1、2、3的数组，然后将其输出到控制台...",
    "usage": {
      "prompt_tokens": 15,
      "completion_tokens": 45,
      "total_tokens": 60
    }
  },
  "code": 200
}
```

### 2. 文件上传接口

**接口地址**：`POST /upload`

**请求头**：
```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

**请求参数**：
```
file: <二进制文件数据>
```

**支持的文件类型**：
- `.txt` - 文本文件
- `.md` - Markdown文件  
- `.pdf` - PDF文档
- `.docx` - Word文档

**文件大小限制**：最大 10MB

**响应示例**：
```json
{
  "success": true,
  "message": "文件上传成功，开始处理",
  "data": {
    "fileId": "550e8400-e29b-41d4-a716-446655440001",
    "filename": "document.pdf",
    "fileSize": 1024000,
    "status": "processing"
  },
  "code": 200
}
```

### 3. 获取用户信息

**接口地址**：`GET /auth/profile`

**请求头**：
```http
Authorization: Bearer <JWT_TOKEN>
```

**响应示例**：
```json
{
  "success": true,
  "message": "获取用户信息成功",
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "testuser",
      "email": "test@example.com",
      "created_at": "2024-01-01T00:00:00Z",
      "last_login": "2024-01-02T10:30:00Z"
    }
  },
  "code": 200
}
```

### 4. 获取上传文件列表

**接口地址**：`GET /files?page=1&limit=10`

**请求头**：
```http
Authorization: Bearer <JWT_TOKEN>
```

**响应示例**：
```json
{
  "success": true,
  "message": "获取文件列表成功",
  "data": {
    "files": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "filename": "document.pdf",
        "fileSize": 1024000,
        "fileType": "pdf",
        "uploadTime": "2024-01-02T10:30:00Z",
        "status": "completed"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 1,
      "totalPages": 1
    }
  },
  "code": 200
}
```

---

## 错误处理规范

### HTTP状态码说明

| 状态码 | 含义 | 使用场景 |
|-------|------|----------|
| 200 | 成功 | 正常请求成功 |
| 201 | 创建成功 | 注册、上传文件成功 |
| 400 | 请求错误 | 参数验证失败 |
| 401 | 未认证 | Token无效或过期 |
| 403 | 权限不足 | 用户被禁用等 |
| 404 | 资源不存在 | 用户不存在等 |
| 409 | 冲突 | 用户名/邮箱已存在 |
| 429 | 请求过频 | 超出限流 |
| 500 | 服务器错误 | 内部错误 |

### 错误响应格式

```json
{
  "success": false,
  "message": "具体错误信息",
  "code": 400,
  "errors": [
    {
      "field": "username",
      "message": "用户名长度必须在3-20个字符之间"
    }
  ]
}
```

### 常见错误示例

**Token过期**：
```json
{
  "success": false,
  "message": "Token已过期，请重新登录",
  "code": 401
}
```

**参数验证失败**：
```json
{
  "success": false,
  "message": "参数验证失败",
  "code": 400,
  "errors": [
    {
      "field": "email",
      "message": "邮箱格式不正确"
    },
    {
      "field": "password",
      "message": "密码长度至少6个字符"
    }
  ]
}
```

---

## JWT Token处理

### Token配置
- **签名算法**：HS256
- **访问Token有效期**：24小时
- **刷新Token有效期**：7天
- **密钥**：使用环境变量 `JWT_SECRET`

### Token验证中间件

所有需要认证的接口都应该验证JWT Token：

1. 检查请求头中是否包含 `Authorization: Bearer <token>`
2. 验证Token签名和有效期
3. 从Token中解析用户信息
4. 检查用户是否存在且状态正常

### 刷新Token机制

1. 访问Token过期时，客户端使用刷新Token获取新的访问Token
2. 刷新Token使用后应该生成新的刷新Token（可选）
3. 用户登出时应该将刷新Token标记为已撤销

---

## 实现示例

### Node.js + Express 示例

```javascript
const express = require('express');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const multer = require('multer');
const { body, validationResult } = require('express-validator');

const app = express();
app.use(express.json());

// JWT中间件
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({
      success: false,
      message: '缺少访问令牌',
      code: 401
    });
  }

  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(401).json({
        success: false,
        message: 'Token无效或已过期',
        code: 401
      });
    }
    req.user = user;
    next();
  });
};

// 注册接口
app.post('/auth/register', [
  body('username').isLength({ min: 3, max: 20 }),
  body('email').isEmail(),
  body('password').isLength({ min: 6 })
], async (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      message: '参数验证失败',
      code: 400,
      errors: errors.array()
    });
  }

  try {
    const { username, email, password } = req.body;
    
    // 检查用户是否已存在
    const existingUser = await getUserByUsername(username);
    if (existingUser) {
      return res.status(409).json({
        success: false,
        message: '用户名已存在',
        code: 409
      });
    }

    // 加密密码
    const passwordHash = await bcrypt.hash(password, 10);
    
    // 创建用户
    const userId = generateUUID();
    await createUser({
      id: userId,
      username,
      email,
      password_hash: passwordHash
    });

    const user = { id: userId, username, email };

    res.status(201).json({
      success: true,
      message: '注册成功',
      data: { user },
      code: 201
    });

  } catch (error) {
    console.error('注册失败:', error);
    res.status(500).json({
      success: false,
      message: '服务器内部错误',
      code: 500
    });
  }
});

// 登录接口
app.post('/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    
    // 验证用户
    const user = await getUserByUsername(username);
    if (!user || !(await bcrypt.compare(password, user.password_hash))) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误',
        code: 401
      });
    }

    // 生成JWT tokens
    const tokenPayload = {
      userId: user.id,
      username: user.username
    };
    
    const token = jwt.sign(tokenPayload, process.env.JWT_SECRET, { expiresIn: '24h' });
    const refreshToken = jwt.sign(tokenPayload, process.env.REFRESH_JWT_SECRET, { expiresIn: '7d' });

    // 保存refresh token
    await saveRefreshToken(user.id, refreshToken);

    // 更新最后登录时间
    await updateLastLogin(user.id);

    res.json({
      success: true,
      message: '登录成功',
      data: {
        token,
        refreshToken,
        user: {
          id: user.id,
          username: user.username,
          email: user.email
        }
      },
      code: 200
    });

  } catch (error) {
    console.error('登录失败:', error);
    res.status(500).json({
      success: false,
      message: '服务器内部错误',
      code: 500
    });
  }
});

// 对话接口
app.post('/ask', authenticateToken, async (req, res) => {
  try {
    const { model, messages, stream, langid } = req.body;
    const userId = req.user.userId;

    if (stream) {
      // 设置SSE响应头
      res.writeHead(200, {
        'Content-Type': 'text/plain',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive'
      });

      // 调用AI服务并流式返回
      const response = await callAIService(model, messages, langid);
      
      response.on('data', (chunk) => {
        res.write(`data: ${JSON.stringify({message: {content: chunk}})}\n\n`);
      });

      response.on('end', () => {
        res.end();
      });

    } else {
      const response = await callAIService(model, messages, langid);
      
      res.json({
        success: true,
        message: '对话完成',
        data: {
          response: response.content,
          usage: response.usage
        },
        code: 200
      });
    }

  } catch (error) {
    console.error('对话请求失败:', error);
    res.status(500).json({
      success: false,
      message: '对话请求失败',
      code: 500
    });
  }
});

// 文件上传配置
const upload = multer({
  dest: 'uploads/',
  limits: { fileSize: 10 * 1024 * 1024 }, // 10MB
  fileFilter: (req, file, cb) => {
    const allowedTypes = ['text/plain', 'text/markdown', 'application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    cb(null, allowedTypes.includes(file.mimetype));
  }
});

// 文件上传接口
app.post('/upload', authenticateToken, upload.single('file'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        success: false,
        message: '请选择要上传的文件',
        code: 400
      });
    }

    const userId = req.user.userId;
    const fileId = generateUUID();
    
    // 保存文件记录到数据库
    await saveFileRecord({
      id: fileId,
      user_id: userId,
      filename: req.file.originalname,
      file_path: req.file.path,
      file_size: req.file.size,
      file_type: req.file.mimetype.split('/')[1]
    });

    // 异步处理文件（向量化等）
    processFileAsync(fileId, req.file.path);

    res.json({
      success: true,
      message: '文件上传成功，开始处理',
      data: {
        fileId,
        filename: req.file.originalname,
        fileSize: req.file.size,
        status: 'processing'
      },
      code: 200
    });

  } catch (error) {
    console.error('文件上传失败:', error);
    res.status(500).json({
      success: false,
      message: '文件上传失败',
      code: 500
    });
  }
});

app.listen(8000, () => {
  console.log('服务器启动在端口 8000');
});
```

### Python Flask 示例

```python
from flask import Flask, request, jsonify, Response
from flask_jwt_extended import JWTManager, create_access_token, create_refresh_token, jwt_required, get_jwt_identity
from werkzeug.security import check_password_hash, generate_password_hash
from werkzeug.utils import secure_filename
import uuid
import json
import time

app = Flask(__name__)
app.config['JWT_SECRET_KEY'] = 'your-secret-key'
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = 86400  # 24小时
app.config['JWT_REFRESH_TOKEN_EXPIRES'] = 604800  # 7天

jwt = JWTManager(app)

@app.route('/auth/register', methods=['POST'])
def register():
    try:
        data = request.get_json()
        username = data.get('username')
        email = data.get('email')
        password = data.get('password')

        # 参数验证
        if not username or len(username) < 3 or len(username) > 20:
            return jsonify({
                'success': False,
                'message': '用户名长度必须在3-20个字符之间',
                'code': 400
            }), 400

        # 检查用户是否存在
        if get_user_by_username(username):
            return jsonify({
                'success': False,
                'message': '用户名已存在',
                'code': 409
            }), 409

        # 创建用户
        user_id = str(uuid.uuid4())
        password_hash = generate_password_hash(password)
        
        create_user({
            'id': user_id,
            'username': username,
            'email': email,
            'password_hash': password_hash
        })

        return jsonify({
            'success': True,
            'message': '注册成功',
            'data': {
                'user': {
                    'id': user_id,
                    'username': username,
                    'email': email
                }
            },
            'code': 201
        }), 201

    except Exception as e:
        return jsonify({
            'success': False,
            'message': '服务器内部错误',
            'code': 500
        }), 500

@app.route('/auth/login', methods=['POST'])
def login():
    try:
        data = request.get_json()
        username = data.get('username')
        password = data.get('password')

        user = get_user_by_username(username)
        if not user or not check_password_hash(user['password_hash'], password):
            return jsonify({
                'success': False,
                'message': '用户名或密码错误',
                'code': 401
            }), 401

        # 生成tokens
        access_token = create_access_token(identity=user['id'])
        refresh_token = create_refresh_token(identity=user['id'])

        return jsonify({
            'success': True,
            'message': '登录成功',
            'data': {
                'token': access_token,
                'refreshToken': refresh_token,
                'user': {
                    'id': user['id'],
                    'username': user['username'],
                    'email': user['email']
                }
            },
            'code': 200
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': '服务器内部错误',
            'code': 500
        }), 500

@app.route('/ask', methods=['POST'])
@jwt_required()
def chat():
    try:
        user_id = get_jwt_identity()
        data = request.get_json()
        
        model = data.get('model')
        messages = data.get('messages')
        stream = data.get('stream', False)
        langid = data.get('langid')

        if stream:
            def generate():
                # 调用AI服务并流式返回
                for chunk in call_ai_service_stream(model, messages, langid):
                    yield f"data: {json.dumps({'message': {'content': chunk}})}\n\n"
                    
            return Response(generate(), mimetype='text/plain')
        else:
            response = call_ai_service(model, messages, langid)
            return jsonify({
                'success': True,
                'message': '对话完成',
                'data': {
                    'response': response['content'],
                    'usage': response.get('usage')
                },
                'code': 200
            })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': '对话请求失败',
            'code': 500
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)
```

---

## 部署说明

### 环境变量配置
```bash
# JWT密钥
JWT_SECRET=your-super-secret-jwt-key
REFRESH_JWT_SECRET=your-super-secret-refresh-key

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=gpt_int
DB_USER=root
DB_PASSWORD=password

# Redis配置（用于缓存）
REDIS_HOST=localhost
REDIS_PORT=6379

# AI服务配置
AI_SERVICE_URL=your-ai-service-endpoint
AI_API_KEY=your-ai-api-key
```

### 安全建议

1. **使用HTTPS**：生产环境必须使用HTTPS
2. **密码加密**：使用bcrypt等安全的哈希算法
3. **限流保护**：实现API限流防止暴力攻击
4. **输入验证**：所有输入都要进行严格验证
5. **日志记录**：记录关键操作的日志
6. **定期清理**：定期清理过期的refresh token

### 测试建议

1. 使用Postman或类似工具测试所有API端点
2. 编写自动化测试用例
3. 进行负载测试确保性能
4. 测试各种错误场景的处理

---

## 联系信息

如有问题请联系开发团队。

**文档版本**：v1.0  
**更新日期**：2024年1月  
**维护者**：GPT-INT开发团队
