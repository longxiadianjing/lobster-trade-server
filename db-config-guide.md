# 数据库配置安全指南

## 当前状态

当前 `application-dev.yml` 中的数据库连接为硬编码明文配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lobster_trade?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

这种方式在开发环境可以接受，但**绝对禁止用于生产环境**。

---

## 生产环境配置要求

### 方案一：环境变量注入（推荐）

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/lobster_trade}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

设置环境变量：
```bash
# Linux/Mac
export DB_URL="jdbc:mysql://prod-host:3306/lobster_trade?useUnicode=true&characterEncoding=utf8&useSSL=true"
export DB_USERNAME="lobster_app"
export DB_PASSWORD="your_secure_password"

# Windows (set permanent)
setx DB_URL "jdbc:mysql://prod-host:3306/lobster_trade"
setx DB_USERNAME "lobster_app"
setx DB_PASSWORD "your_secure_password"
```

### 方案二：配置中心（适合集群部署）

使用 Nacos/Apollo 等配置中心，统一管理所有环境的数据库配置。

### 方案三：K8s Secrets（如果使用容器部署）

```yaml
# k8s secret
apiVersion: v1
kind: Secret
metadata:
  name: lobster-db-secret
type: Opaque
stringData:
  DB_USERNAME: lobster_app
  DB_PASSWORD: your_secure_password
```

---

## 安全规范

1. **禁止在代码仓库中存储明文密码** — 使用 `.gitignore` 排除 `application-prod.yml`
2. **数据库账号最小权限** — 应用账号只给 DML + DDL 权限，不要给 DROP 权限
3. **生产库禁止外网访问** — 只允许内网/VPN 访问，限制源 IP
4. **定期轮转密码** — 建议每90天更换一次生产数据库密码
5. **敏感字段加密存储** — 银行卡号、身份证等使用 AES/RSA 加密后存入数据库

---

## 配置优先级

Spring Boot 配置加载优先级（从高到低）：
1. 命令行参数 `--spring.datasource.password=xxx`
2. 环境变量 `SPRING_DATASOURCE_PASSWORD`
3. 配置文件 `application-{profile}.yml`

开发环境使用 `-dev` profile，生产环境使用 `-prod` profile。
