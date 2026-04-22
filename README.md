# 智能电网隐私保护与数据安全聚合系统

## 项目概述

本系统实现了一套"智能电表—雾节点—控制中心"三层架构的隐私保护数据聚合系统，具有以下核心功能：

- 登录与身份管理界面（管理员认证）
- 电力数据模拟/导入与终端处理界面
- 数据编码、同态加密、数字签名等密码处理模块
- 加密数据上报、雾端聚合与中心解密展示模块
- 聚合结果完整性验证（双层签名）
- 重放检测与日志审计模块

## 技术栈

### 后端
- Spring Boot 2.7.x
- Java 17
- MySQL 8.0
- BouncyCastle 1.70
- Spring Data JPA

### 前端
- React 18.2
- ECharts 5.4
- axios 1.4

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 16+

## 快速开始

### 1. 数据库配置

确保MySQL服务已启动，并创建数据库：

```sql
CREATE DATABASE smartgrid_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

或者直接运行系统，数据库会自动创建。

### 2. 后端启动

```bash
cd backend

# 使用Maven编译项目
mvn clean install

# 运行项目
mvn spring-boot:run
```

后端服务将运行在 http://localhost:8080

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm start
```

前端应用将运行在 http://localhost:3000

### 4. 访问系统

打开浏览器访问 http://localhost:3000，使用默认账号登录：

- 用户名：admin
- 密码：admin123

## 系统功能

### 1. 仪表盘
- 查看系统状态
- 终端设备统计
- 快速操作入口

### 2. 终端管理
- 查看所有终端设备状态
- 启动/停止单个或全部终端
- 实时监控终端上报状态

### 3. 数据统计
- 查看聚合统计数据
- 实时数据趋势图表
- 历史记录查询

### 4. 审计日志
- 查看系统安全事件
- 按级别、模块筛选
- 导出CSV报告

## 核心密码学模块

### Paillier同态加密
- 支持加密、解密、同态加法
- 1024位密钥长度（可配置）
- 支持密文聚合

### EC-Schnorr签名
- 基于secp256k1曲线
- 签名和验签功能
- 用于终端和雾节点签名

### 数据编码
- 超递增序列编码
- 多维数据打包
- 支持5维电力数据

## 系统架构

```
┌─────────────────┐
│   控制中心      │ ← 接收聚合结果、验签解密
└────────┬────────┘
         ↑
┌────────┴────────┐
│    雾节点        │ ← 验签、重放检测、聚合
└────────┬────────┘
         ↑
┌────────┴────────┐
│   终端模拟器    │ ← 数据采集、加密、签名
└─────────────────┘
```

## 配置说明

主要配置项位于 `backend/src/main/resources/application.yml`：

- `smartgrid.meter.count`: 终端数量（默认10）
- `smartgrid.report.interval`: 上报间隔（默认60000ms）
- `smartgrid.aggregate.window`: 聚合窗口（默认300000ms）
- `smartgrid.paillier.key-length`: Paillier密钥长度（默认1024位）
- `jwt.secret`: JWT签名密钥
- `jwt.expiration`: Token有效期（默认24小时）

## 项目结构

```
smartgrid-system/
├── backend/
│   ├── src/main/java/com/smartgrid/
│   │   ├── SmartGridApplication.java
│   │   ├── auth/          # 认证模块
│   │   ├── crypto/        # 密码学模块
│   │   ├── meter/         # 终端模拟器
│   │   ├── fog/           # 雾节点
│   │   ├── center/        # 控制中心
│   │   ├── audit/         # 审计日志
│   │   ├── entity/        # 实体类
│   │   ├── controller/    # 控制器
│   │   ├── repository/    # 数据访问
│   │   ├── dto/           # 数据传输对象
│   │   └── config/        # 配置类
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── schema.sql
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── pages/         # 页面组件
│   │   ├── services/      # API服务
│   │   ├── App.js
│   │   └── index.js
│   └── package.json
└── README.md
```

## 安全特性

1. **隐私保护**: 雾节点无法解密个体用户数据
2. **完整性验证**: 签名验证机制检测篡改行为
3. **抗重放攻击**: 序列号+时间戳组合检测
4. **审计追踪**: 所有安全事件记录

## 默认配置

数据库连接（可在application.yml中修改）：
- URL: jdbc:mysql://localhost:3306/smartgrid_db
- 用户名: root
- 密码: root

## 注意事项

1. 首次运行会自动创建数据库表
2. 默认管理员账号请在首次登录后修改密码
3. 生产环境请修改JWT密钥和数据库密码
4. 确保MySQL服务已启动

## License

MIT License
