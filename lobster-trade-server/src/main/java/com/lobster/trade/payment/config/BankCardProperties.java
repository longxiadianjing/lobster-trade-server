package com.lobster.trade.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 银行卡支付配置属性（预留，后期接入拉卡拉/易宝等）
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.bankcard")
public class BankCardProperties {
    /** 是否启用 */
    private boolean enabled = false;

    /** 支付平台：laKaLa / EasyPay / unionPay */
    private String platform;

    /** 商户号 */
    private String mchId;

    /** 应用ID */
    private String appId;

    /** 私钥 */
    private String privateKey;

    /** 公钥 */
    private String publicKey;

    /** 异步回调地址 */
    private String notifyUrl;
}