# 龙虾道具交易平台 - 完整部署教程

**版本：** 1.0.0  
**最后更新：** 2026-05-10

---

## 目录
1. 环境要求
2. 服务器配置
3. 数据库部署
4. 后端部署
5. 前端部署
6. 支付接口配置（支付宝/微信/银行卡）
7. 常见问题

---

## 1. 环境要求

### 硬件要求
- CPU: 2核+
- 内存: 4GB+
- 磁盘: 50GB+

### 软件要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- Maven 3.8+

---

## 2. 服务器配置

### 2.1 安装 JDK 17
```bash
# Ubuntu/Debian
apt update && apt install openjdk-17-jdk

# CentOS
yum install java-17-openjdk
```

### 2.2 安装 MySQL 8.0
```bash
# Ubuntu/Debian
apt install mysql-server

# 初始化
mysql_secure_installation
```

### 2.3 安装 Redis
```bash
apt install redis-server
systemctl enable redis
```

---

## 3. 数据库部署

### 3.1 创建数据库
```sql
CREATE DATABASE lobster_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lobster'@'%' IDENTIFIED BY '你的强密码';
GRANT ALL PRIVILEGES ON lobster_trade.* TO 'lobster'@'%';
FLUSH PRIVILEGES;
```

### 3.2 执行建表脚本
```bash
mysql -u lobster -p lobster_trade < sql/init_schema.sql
# 导入增强表（新增功能）
mysql -u lobster -p lobster_trade < sql/enhanced_schema.sql
```

### 3.3 初始数据
（如果有 init_data.sql，执行之）

---

## 4. 后端部署

### 4.1 打包
```bash
cd lobster-trade-server
mvn clean package -DskipTests
```

### 4.2 启动脚本
创建 `start.sh`：
```bash
#!/bin/bash
export JAVA_OPTS="-Xms512m -Xmx1024m"
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=你的数据库IP
export DB_PORT=3306
export DB_NAME=lobster_trade
export DB_USERNAME=lobster
export DB_PASSWORD=你的数据库密码
export REDIS_HOST=你的Redis IP
export REDIS_PORT=6379
export JWT_SECRET=你的JWT密钥（至少32位随机字符串）
export PAYMENT_SECRET=你的支付密钥
java $JAVA_OPTS -jar lobster-trade-server-1.0.0-SNAPSHOT.jar
```

### 4.3 系统服务配置
创建 systemd 服务：`/etc/systemd/system/lobster-trade.service`

---

## 5. 前端部署

### 5.1 构建
```bash
cd lobster-trade-web
npm install
npm run build
```

### 5.2 Nginx 配置
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;
    
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
    }
    
    location /uploads/ {
        alias /path/to/uploads/;
    }
    
    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

## 6. 支付接口配置（重点）

### 6.1 环境变量配置（核心）

所有支付配置通过环境变量注入，**禁止硬编码**。

```bash
# ========== 支付宝 ==========
export ALIPAY_ENABLED=true
export ALIPAY_APP_ID=你的支付宝应用AppID
export ALIPAY_PRIVATE_KEY=你的应用私钥（PKCS8格式RSA2）
export ALIPAY_PUBLIC_KEY=支付宝公钥
export ALIPAY_NOTIFY_URL=https://your-domain.com/api/payment/alipay/notify
export ALIPAY_RETURN_URL=https://your-domain.com/payment/result
export ALIPAY_SANDBOX=false   # 生产环境 false

# ========== 微信支付 ==========
export WECHAT_ENABLED=true
export WECHAT_APP_ID=你的微信AppID
export WECHAT_MCH_ID=你的商户号
export WECHAT_PRIVATE_KEY=你的APIv3私钥内容（PEM格式，去掉头尾）
export WECHAT_CERT_SERIAL_NO=平台证书序列号
export WECHAT_CERT_CONTENT=平台证书内容（PEM格式，完整内容）
export WECHAT_NOTIFY_URL=https://your-domain.com/api/payment/wechat/notify

# ========== 银行卡/聚合支付 ==========
export BANKCARD_ENABLED=false   # 未接入时 false
export BANKCARD_PLATFORM=       # laKaLa/EasyPay/unionPay
export BANKCARD_MCH_ID=
export BANKCARD_APP_ID=
export BANKCARD_NOTIFY_URL=
```

### 6.2 支付宝申请步骤

1. 登录 [支付宝开放平台](https://open.alipay.com/)
2. 创建应用 → 添加「电脑网站支付」产品
3. 配置 RSA2 密钥：
   - 生成应用私钥：`openssl genrsa -out app_private_key.pem 2048`
   - 转换 PKCS8：`openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in app_private_key.pem -out app_private_key_pkcs8.pem`
   - 获取支付宝公钥：支付宝后台「应用公钥」页面
4. 填入环境变量

### 6.3 微信支付申请步骤

1. 登录 [微信支付商户平台](https://pay.weixin.qq.com/)
2. 获取商户号（mchId）和 AppID
3. 申请证书（APIv3）：
   - 商户后台 → 账户中心 → API安全 → 申请证书
   - 下载平台证书，记录序列号
4. 获取私钥：商户后台 → API安全 → 设置密钥（APIv3）
5. 配置环境变量

### 6.4 银行卡/聚合支付（预留）

当前系统已预留 `BankCardPayGateway`，后期接入只需：
1. 申请拉卡拉/易宝等商户号
2. 配置 `BANKCARD_*` 环境变量
3. 设置 `BANKCARD_ENABLED=true`
4. 无需修改任何业务代码

### 6.5 生产环境检查清单

- [ ] 所有 `application.yml` 中的硬编码密钥已移除
- [ ] 生产环境使用 `SPRING_PROFILES_ACTIVE=prod`
- [ ] 数据库密码为强密码
- [ ] JWT_SECRET 为随机生成（至少32字符）
- [ ] 支付宝/微信回调地址已改为正式域名
- [ ] HTTPS 已配置（支付宝/微信要求 HTTPS 回调）

---

## 7. 常见问题

### Q1: 支付回调失败
检查：
1. 回调地址是否公网可访问
2. 支付宝公钥是否正确（注意是「支付宝公钥」不是「应用公钥」）
3. 微信平台证书是否配置
4. 日志中 `[ALIPAY_NOTIFY]` 或 `[WECHAT_NOTIFY]` 的具体错误

### Q2: 微信沙箱环境
测试时设置 `WECHAT_SANDBOX=true`，但注意沙箱签名不同

### Q3: 数据库连接失败
检查 DB_HOST、DB_PORT、DB_USERNAME、DB_PASSWORD 环境变量

### Q4: Redis 连接失败
检查 REDIS_HOST、REDIS_PORT、REDIS_PASSWORD

---

## 附录：关键文件路径

| 文件 | 说明 |
|------|------|
| `application.yml` | 主配置（所有敏感项已移至环境变量） |
| `payment-config-template.yml` | 支付配置模板（仅供参考） |
| `sql/enhanced_schema.sql` | 增强建表脚本 |
| `db-config-guide.md` | 数据库配置安全规范 |
| `DEPLOYMENT_GUIDE.md` | 本文档 |

---

**安全提醒：生产环境绝对禁止将私钥/AppID/密码等敏感信息写入代码库！**