package com.lobster.trade.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道枚举
 */
@Getter
@AllArgsConstructor
public enum PayChannel {
    ALIPAY("alipay", "支付宝"),
    WECHAT("wechat", "微信支付"),
    BANK_CARD("bank_card", "银行卡"),
    UNION_PAY("union_pay", "云闪付"),
    MOCK("mock", "模拟支付");

    private final String code;
    private final String name;

    public static PayChannel fromCode(String code) {
        if (code == null) return null;
        for (PayChannel channel : values()) {
            if (channel.getCode().equals(code)) {
                return channel;
            }
        }
        return null;
    }
}