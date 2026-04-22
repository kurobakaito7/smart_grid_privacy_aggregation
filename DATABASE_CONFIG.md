# 数据库配置说明

## MySQL连接配置

后端应用默认配置位于：`backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smartgrid_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
    username: root
    password: YOUR_PASSWORD_HERE  # 需要修改为您的MySQL密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 修改数据库密码

### 方法1：直接修改配置文件

编辑 `backend/src/main/resources/application.yml`，将 `password:` 后面的值修改为您的MySQL root用户密码。

### 方法2：使用环境变量

```bash
export SPRING_DATASOURCE_PASSWORD=your_password
mvn spring-boot:run
```

### 方法3：在MySQL中设置空密码

如果您想使用空密码连接MySQL，可以执行：

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
FLUSH PRIVILEGES;
```

## 验证MySQL连接

在启动应用前，可以先验证MySQL是否可连接：

```bash
mysql -u root -p -e "SELECT 1;"
```

## 常见问题

### Access denied for user 'root'@'localhost'

这表示密码不正确。请确认：
1. MySQL服务正在运行
2. 用户名和密码正确
3. 用户有权限访问数据库

### Unknown database 'smartgrid_db'

应用会自动创建数据库，但需要连接MySQL后才能创建。请确保先能连接到MySQL。

## 创建专用数据库用户（推荐）

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smartgrid_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER 'smartgrid'@'localhost' IDENTIFIED BY 'smartgrid123';
GRANT ALL PRIVILEGES ON smartgrid_db.* TO 'smartgrid'@'localhost';
FLUSH PRIVILEGES;
```

然后修改配置文件使用新用户：

```yaml
spring:
  datasource:
    username: smartgrid
    password: smartgrid123
```
