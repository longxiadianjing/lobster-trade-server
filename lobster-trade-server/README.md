# 龙虾道具交易平台 - 后端服务

## 技术栈
- SpringBoot 2.7
- MyBatis-Plus
- MySQL 8.0
- Redis
- JWT

## 项目结构
```
lobster-trade-server/
├── src/main/java/com/lobster/trade/
│   ├── config/         # 配置类
│   ├── controller/    # REST控制器
│   ├── service/       # 业务逻辑
│   ├── mapper/        # 数据访问
│   ├── model/         # 数据模型
│   ├── common/        # 公共组件
│   └── util/          # 工具类
├── src/main/resources/
│   └── application.yml
├── db/                 # 数据库脚本
│   ├── init_database.sql  # 创建数据库
│   ├── init.sql           # 建表脚本（30张表）
│   └── seed_data.sql      # 基础数据
├── pom.xml
└── README.md
```

## 部署文档

### 环境要求
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6+

### 后端部署
1. **创建数据库**
   ```sql
   mysql -u root -p < db/init_database.sql
   ```

2. **执行建表脚本**
   ```sql
   mysql -u root -p lobster_trade < db/init.sql
   ```

3. **初始化基础数据**（可选）
   ```sql
   mysql -u root -p lobster_trade < db/seed_data.sql
   ```

4. **修改数据库配置**
   编辑 `src/main/resources/application-dev.yml`，确认以下配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/lobster_trade?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
       username: root
       password: root    # ← 修改为你的MySQL密码
     redis:
       host: localhost
       port: 6379
       password:         # ← 如有密码请填写
   ```

5. **编译打包**
   ```bash
   mvn clean package -DskipTests
   ```

6. **启动服务**
   ```bash
   java -jar target/lobster-trade-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
   ```

### 前端部署（用户端）
```bash
cd lobster-trade-web
npm install
npm run build
# 或开发模式
npm run dev
```

### 前端部署（管理后台）
```bash
cd lobster-trade-admin
npm install
npm run build
```

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 运营管理员 | operator | admin123 |
| 买家 | 13812340001 | Test123456 |
| 卖家 | 13900002222 | Test123456 |
| 卖家 | 13900002223 | Test123456 |

### API 端口
- 后端 API：**8080**
- 用户端前端：**5174**
- 管理后台前端：**8090**


### 数据库信息
- 数据库名：`lobster_trade`
- 字符集：`utf8mb4_unicode_ci`
- 表数量：30张

### 数据库表清单
1. `admin` - 管理员表（老版本）
2. `admin_audit_log` - 管理员操作审计日志
3. `admin_role` - 管理员角色表
4. `admin_user` - 管理员表
5. `coupon` - 优惠券表
6. `cs_message` - 客服消息表
7. `cs_session` - 客服会话表
8. `game` - 游戏表（旧版本）
9. `game_category` - 游戏分类表
10. `hot_search_word` - 热搜词表
11. `im_message` - IM消息表
12. `im_session` - IM会话表
13. `order_progress` - 订单进度记录表
14. `platform_announcement` - 平台公告表
15. `product` - 商品表
16. `product_category` - 商品服务分类表
17. `product_recommend_slot` - 商品推荐位表
18. `service_provider_certification` - 服务商认证表
19. `service_ticket` - 客服工单表
20. `sms_code` - 短信验证码表
21. `sys_notification` - 系统通知表
22. `trade_order` - 订单表
23. `trade_review` - 评价表
24. `user` - 用户表
25. `user_coupon` - 用户优惠券表
26. `user_login_device` - 用户登录设备表
27. `user_real_name` - 用户实名表
28. `user_session` - 用户会话表
29. `wallet` - 钱包表
30. `wallet_transaction` - 钱包流水表

## 快速开始
1. 创建数据库 `lobster_trade`
2. 执行 `db/init.sql` 初始化表结构
3. （可选）执行 `db/seed_data.sql` 初始化测试数据
4. 修改 `application-dev.yml` 配置数据库连接
5. 运行 `LobsterTradeApplication.java`

## 接口文档
启动后访问：http://localhost:8080/swagger-ui.html
