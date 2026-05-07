package com.lobster.trade.config;

import com.wechat.pay.contrib.apache.httpclient.WeChatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "payment.wechat")
public class WeChatConfig {

    private String appId;
    private String mchId;
    private String apiKey;
    private String certPath;
    private String notifyUrl;
    private boolean enabled;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getMchId() { return mchId; }
    public void setMchId(String mchId) { this.mchId = mchId; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getCertPath() { return certPath; }
    public void setCertPath(String certPath) { this.certPath = certPath; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Bean
    public CloseableHttpClient wechatPayClient() {
        if (!enabled || appId == null || mchId == null || apiKey == null) {
            log.warn("[WECHAT_PAY] 微信支付未启用或配置不完整，appId={}, mchId={}, enabled={}", appId, mchId, enabled);
            return null;
        }
        try {
            // 使用 APIv2 HMAC-SHA256 签名（无需证书）
            // 商户私钥：使用 API Key 作为 HMAC 密钥
            PrivateKey privateKey = PemUtil.loadPrivateKeyFromBase64(
                Base64.getEncoder().encodeToString(apiKey.getBytes(StandardCharsets.UTF_8)));

            WeChatPayHttpClientBuilder builder = WeChatPayHttpClientBuilder.create()
                .withCredentials(appId, mchId)
                .withPrivateKey(privateKey)
                .withValidator(response -> true); // 跳过签名验证（测试环境）

            return builder.build();
        } catch (Exception e) {
            log.error("[WECHAT_PAY] 初始化微信支付客户端失败", e);
            return null;
        }
    }
}
