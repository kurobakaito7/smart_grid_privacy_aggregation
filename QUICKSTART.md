# 智能电网隐私保护与数据安全聚合系统

## 重要提示：数据库配置

**在启动系统前，您需要配置MySQL数据库连接！**

请查看 [DATABASE_CONFIG.md](DATABASE_CONFIG.md) 了解如何配置数据库连接。

## 快速开始

### 1. 配置数据库

编辑 `backend/src/main/resources/application.yml`，修改MySQL连接密码：

```yaml
spring:
  datasource:
    password: 您的MySQL密码
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 3. 启动前端

```bash
cd frontend
npm install
npm start
```

### 4. 访问系统

打开浏览器访问 http://localhost:3000

默认登录账号：admin / admin123

## 项目概述

本系统实现了一套"智能电表—雾节点—控制中心"三层架构的隐私保护数据聚合系统。

## 系统功能

- ✅ 登录与身份管理（JWT认证）
- ✅ 电力数据模拟与终端处理
- ✅ Paillier同态加密
- ✅ EC-Schnorr数字签名
- ✅ 雾节点聚合与重放检测
- ✅ 控制中心解密与统计
- ✅ 审计日志
- ✅ 实时数据图表

## 技术栈

- 后端：Spring Boot 2.7.x, Java 17, MySQL 8.0, BouncyCastle
- 前端：React 18.2, ECharts 5.4

## 详细文档

- [开发文档](docs/智能电网隐私保护系统开发.md)
- [数据库配置说明](DATABASE_CONFIG.md)
