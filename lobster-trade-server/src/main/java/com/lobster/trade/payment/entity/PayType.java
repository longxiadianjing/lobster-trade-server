package com.lobster.trade.payment.entity;

/**
 * 支付类型枚举
 */
public enum PayType {
    RECHARGE("recharge"),   // 余额充值
    ORDER("order");         // 订单支付

    private final String value;

    PayType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PayType fromValue(String value) {
        if (value == null) return null;
        for (PayType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}