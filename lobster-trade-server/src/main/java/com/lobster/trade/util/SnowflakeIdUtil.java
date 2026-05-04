package com.lobster.trade.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SnowflakeIdUtil {

    /**
     * 生成交易流水号
     */
    public static String generateTransNo() {
        return "TX" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 生成订单号
     */
    public static String generateOrderNo() {
        return "OD" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 生成UUID（无中划线）
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
