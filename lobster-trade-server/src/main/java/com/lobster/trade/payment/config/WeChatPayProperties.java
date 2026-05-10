package com.lobster.trade.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性（APIv3 RSA）
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.wechat")
public class WeChatPayProperties {
    /** 是否启用 */
    private boolean enabled = false;

    /** 应用ID */
    private String appId;

    /** 商户号 */
    private String mchId;

    /** APIv3 私钥内容（直接配置，不走文件，优先级高于 privateKeyPath） */
    private String privateKey;

    /** 私钥文件路径（classpath 或绝对路径，可选） */
    private String privateKeyPath;

    /** APIv2 密钥（HMAC-SHA256，非必填） */
    private String apiKey;

    /** 平台证书序列号（用于回调验签） */
    private String certSerialNo;

    /** 平台证书内容（PEM 格式，用于 APIv3 回调验签） */
    private String certContent;

    /** 异步回调地址 */
    private String notifyUrl;

    /** 开发模式跳过验签（仅本地测试用，生产必须 false） */
    private boolean devSkipVerify = false;
}
