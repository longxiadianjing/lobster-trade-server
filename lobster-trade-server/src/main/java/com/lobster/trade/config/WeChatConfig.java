package com.lobster.trade.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 微信支付配置（APIv3 RSA）
 * 配置项前缀：payment.wechat
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "payment.wechat")
public class WeChatConfig {

    /** 微信应用 AppID */
    private String appId = "wx305b929f776078d8";

    /** 商户号 */
    private String mchId = "1715080856";

    /** APIv3 私钥文件路径（classpath 或文件系统） */
    private String privateKeyPath = "classpath:wechat_private_key.pem";

    /** APIv2 密钥（HMAC-SHA256，非必填，APIv3 不需要） */
    private String apiKey;

    /** 平台证书路径（用于回调验签） */
    private String certPath;

    /** 证书序列号 */
    private String certSerialNo = "5E7EAD6F807398EE2B39958E5E1B3CDDE63AB803";

    /** 回调地址 */
    private String notifyUrl = "http://localhost:8080/api/payment/wechat/notify";

    /** 是否启用 */
    private boolean enabled = true;

    /** APIv3 私钥内容（直接注入） */
    private String privateKey;

    /** 懒加载私钥内容 */
    private String privateKeyCache;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getMchId() { return mchId; }
    public void setMchId(String mchId) { this.mchId = mchId; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getCertPath() { return certPath; }
    public void setCertPath(String certPath) { this.certPath = certPath; }
    public String getCertSerialNo() { return certSerialNo; }
    public void setCertSerialNo(String certSerialNo) { this.certSerialNo = certSerialNo; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getPrivateKeyPath() { return privateKeyPath; }
    public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; if (privateKey != null && !privateKey.isEmpty()) this.privateKeyCache = privateKey; }

    /**
     * 获取私钥内容（从文件懒加载，只读一次）
     */
    public String getPrivateKey() {
        if (privateKeyCache != null) return privateKeyCache;
        try {
            String path = privateKeyPath;
            if (path.startsWith("classpath:")) {
                String resourcePath = path.substring("classpath:".length());
                ClassPathResource resource = new ClassPathResource(resourcePath);
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    privateKeyCache = reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                privateKeyCache = java.nio.file.Files.readString(
                    java.nio.file.Paths.get(path), StandardCharsets.UTF_8);
            }
            log.info("[WECHAT_PAY] 私钥加载成功，mchId={}, appId={}", mchId, appId);
        } catch (Exception e) {
            log.error("[WECHAT_PAY] 私钥加载失败: {}", e.getMessage());
        }
        return privateKeyCache;
    }
}
