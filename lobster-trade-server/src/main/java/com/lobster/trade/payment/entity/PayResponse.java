package com.lobster.trade.payment.entity;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一支付响应
 */
@Data
@Builder
public class PayResponse {
    /** 平台支付单号 */
    private String paymentNo;

    /** 支付渠道 */
    private PayChannel channel;

    /** 支付状态：SUCCESS / FAIL / PENDING */
    private PayStatus status;

    /** 支付金额 */
    private BigDecimal amount;

    /** 跳转链接（支付宝扫码/微信H5等） */
    private String payUrl;

    /** 二维码内容 */
    private String qrCodeData;

    /** HTML表单（支付宝PC支付） */
    private String htmlForm;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 第三方交易流水号 */
    private String transactionId;

    /** 错误信息 */
    private String errorMessage;
}