package com.lobster.trade.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置（APIv2 HMAC-SHA256）
 * 配置项：payment.wechat.appId / mchId / apiKey / notifyUrl / enabled
 */
@Slf4j
@Component
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
}
