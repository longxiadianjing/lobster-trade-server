package com.lobster.trade.payment.entity;

/**
 * 支付状态枚举
 */
public enum PayStatus {
    PENDING("PENDING", "待支付"),
    SUCCESS("SUCCESS", "支付成功"),
    FAIL("FAIL", "支付失败"),
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String description;

    PayStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PayStatus fromCode(String code) {
        if (code == null) return null;
        for (PayStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}