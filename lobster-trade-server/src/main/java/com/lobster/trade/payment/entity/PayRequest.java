package com.lobster.trade.payment.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 统一支付请求
 */
@Data
public class PayRequest {
    /** 用户ID */
    private Long userId;

    /** 平台支付单号 */
    private String paymentNo;

    /** 支付渠道 */
    private PayChannel channel;

    /** 支付类型：RECHARGE / ORDER */
    private PayType type;

    /** 关联订单ID（充值时为null，订单支付时为orderId） */
    private Long orderId;

    /** 支付金额 */
    private BigDecimal amount;

    /** 描述 */
    private String description;

    /** 前端回跳地址 */
    private String returnUrl;

    /** 异步回调地址 */
    private String notifyUrl;

    /** 扩展参数（JSON字符串） */
    private String extraData;
}