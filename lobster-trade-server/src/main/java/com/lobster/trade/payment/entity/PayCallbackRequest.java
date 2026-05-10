package com.lobster.trade.payment.entity;

import lombok.Data;
import java.util.Map;

/**
 * 统一回调请求
 */
@Data
public class PayCallbackRequest {
    /** 支付渠道 */
    private PayChannel channel;

    /** 平台支付单号 */
    private String paymentNo;

    /** 第三方交易号 */
    private String transactionId;

    /** 支付状态 */
    private String tradeStatus;

    /** 实际支付金额（可能与发起金额不一致，需校验） */
    private String totalAmount;

    /** 支付成功时间 */
    private String paidTime;

    /** 所有原始参数 */
    private Map<String, String> allParams;

    /** 签名（支付宝用sign字段，微信用-signature） */
    private String signature;
}