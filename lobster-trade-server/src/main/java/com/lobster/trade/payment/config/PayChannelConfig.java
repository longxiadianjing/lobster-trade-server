package com.lobster.trade.payment.config;

import com.lobster.trade.payment.entity.PayChannel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 统一支付渠道配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment")
public class PayChannelConfig {

    /** 统一支付密钥（用于内部签名防伪） */
    private String secret;

    /** 支付宝配置 */
    private AlipayProperties alipay = new AlipayProperties();

    /** 微信支付配置 */
    private WeChatPayProperties wechat = new WeChatPayProperties();

    /** 银行卡支付配置 */
    private BankCardProperties bankcard = new BankCardProperties();

    /**
     * 检查指定渠道是否启用
     */
    public boolean isChannelEnabled(PayChannel channel) {
        switch (channel) {
            case ALIPAY:
                return alipay.isEnabled();
            case WECHAT:
                return wechat.isEnabled();
            case BANK_CARD:
            case UNION_PAY:
                return bankcard.isEnabled();
            case MOCK:
                return true; // Mock始终可用
            default:
                return false;
        }
    }
}