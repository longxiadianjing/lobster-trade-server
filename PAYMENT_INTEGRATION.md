# 支付接口对接配置手册（详细版）

**适用版本：** 龙虾道具交易平台 v1.0.0+  
**最后更新：** 2026-05-10

---

## 一、配置位置速查表

| 配置项 | 环境变量 | application.yml 路径 |
|--------|---------|---------------------|
| 支付宝启用 | ALIPAY_ENABLED | payment.alipay.enabled |
| 支付宝 AppID | ALIPAY_APP_ID | payment.alipay.appId |
| 支付宝私钥 | ALIPAY_PRIVATE_KEY | payment.alipay.privateKey |
| 支付宝公钥 | ALIPAY_PUBLIC_KEY | payment.alipay.alipayPublicKey |
| 支付宝回调 | ALIPAY_NOTIFY_URL | payment.alipay.notifyUrl |
| 微信启用 | WECHAT_ENABLED | payment.wechat.enabled |
| 微信 AppID | WECHAT_APP_ID | payment.wechat.appId |
| 微信商户号 | WECHAT_MCH_ID | payment.wechat.mchId |
| 微信私钥 | WECHAT_PRIVATE_KEY | payment.wechat.privateKey |
| 微信证书序列号 | WECHAT_CERT_SERIAL_NO | payment.wechat.certSerialNo |
| 微信平台证书 | WECHAT_CERT_CONTENT | payment.wechat.certContent |
| 微信回调 | WECHAT_NOTIFY_URL | payment.wechat.notifyUrl |

---

## 二、支付宝配置详解

### 2.1 快速配置

在服务器环境变量中添加：
```bash
export ALIPAY_ENABLED=true
export ALIPAY_APP_ID=2021006151687443
export ALIPAY_PRIVATE_KEY=你的应用私钥（PKCS8 PEM格式）
export ALIPAY_PUBLIC_KEY=支付宝公钥
export ALIPAY_NOTIFY_URL=https://你的域名/api/payment/alipay/notify
```

### 2.2 私钥格式说明

必须使用 PKCS8 格式的 RSA2 私钥。格式示例：
```
-----BEGIN PRIVATE KEY-----
MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQC...
...
-----END PRIVATE KEY-----
```

### 2.3 沙箱测试

测试时启用沙箱：
```bash
export ALIPAY_SANDBOX=true
```
沙箱环境支付宝网关：`https://openapi-sandbox.dl.alipaydev.com/gateway.do`

---

## 三、微信支付配置详解

### 3.1 快速配置

```bash
export WECHAT_ENABLED=true
export WECHAT_APP_ID=wx开头的AppID
export WECHAT_MCH_ID=8位数字商户号
export WECHAT_PRIVATE_KEY=APIv3私钥内容（PEM格式，去掉头尾）
export WECHAT_CERT_SERIAL_NO=平台证书序列号（40位十六进制）
export WECHAT_CERT_CONTENT=平台证书完整PEM内容
export WECHAT_NOTIFY_URL=https://你的域名/api/payment/wechat/notify
```

### 3.2 APIv3 私钥格式

APIv3 使用 PKCS#8 格式私钥，格式示例：
```
-----BEGIN PRIVATE KEY-----
MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQC...
...
-----END PRIVATE KEY-----
```

### 3.3 平台证书获取

微信支付 APIv3 需要平台证书用于回调验签：
1. 商户后台 → 账户中心 → API安全 → 申请证书
2. 下载 `.p12` 或 `.pem` 证书文件
3. 转换为 PEM 格式，获取证书序列号（openssl 提取）

---

## 四、银行卡支付预留接口

系统已预留银行卡/聚合支付接口（`BankCardPayGateway`），后期接入：

```bash
export BANKCARD_ENABLED=true
export BANKCARD_PLATFORM=laKaLa   # 或 EasyPay / unionPay
export BANKCARD_MCH_ID=商户号
export BANKCARD_APP_ID=应用ID
export BANKCARD_NOTIFY_URL=https://你的域名/api/payment/bankcard/notify
```

---

## 五、回调地址配置

### 5.1 支付宝回调地址
```
https://你的域名/api/payment/alipay/notify
```

### 5.2 微信支付回调地址
```
https://你的域名/api/payment/wechat/notify
```

### 5.3 回调地址要求
- 必须 HTTPS
- 公网可访问
- 不带 Query String 参数
- 微信支付要求返回 XML 格式

---

## 六、测试验证

### 6.1 支付宝测试
1. 启用沙箱：`ALIPAY_SANDBOX=true`
2. 用测试账号登录支付宝沙箱环境
3. 完成支付，观察日志 `[ALIPAY_NOTIFY]`

### 6.2 微信支付测试
1. 沙箱模式：`WECHAT_SANDBOX=true`
2. 使用微信支付沙箱工具生成签名
3. 完成支付，观察日志 `[WECHAT_NOTIFY]`

### 6.3 日志关键词
- `[ALIPAY_NOTIFY] 签名验证通过` — 支付宝验签成功
- `[ALIPAY_NOTIFY] 支付成功` — 支付成功
- `[WECHAT_NOTIFY] 微信支付成功` — 微信支付成功
- `[PAYMENT] 创建充值支付单` — 支付单创建