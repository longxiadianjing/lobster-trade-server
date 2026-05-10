package com.lobster.trade.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayProperties {
    /** 是否启用 */
    private boolean enabled = false;

    /** 应用ID */
    private String appId;

    /** 应用私钥（PKCS8格式，RSA2） */
    private String privateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 异步回调地址 */
    private String notifyUrl;

    /** 前端回跳地址 */
    private String returnUrl;

    /** 是否沙箱环境 */
    private boolean sandbox = false;
}