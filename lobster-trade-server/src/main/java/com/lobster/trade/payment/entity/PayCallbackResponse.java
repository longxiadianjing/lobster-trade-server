package com.lobster.trade.payment.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 统一回调响应
 */
@Data
@Builder
public class PayCallbackResponse {
    /** 是否成功 */
    private boolean success;

    /** 平台支付单号 */
    private String paymentNo;

    /** 第三方交易号 */
    private String transactionId;

    /** 响应内容（支付宝返回"success"，微信返回XML） */
    private String responseBody;

    /** 错误码 */
    private String errorCode;

    /** 错误信息 */
    private String errorMessage;

    public static PayCallbackResponse success(String paymentNo, String transactionId, String responseBody) {
        return PayCallbackResponse.builder()
            .success(true)
            .paymentNo(paymentNo)
            .transactionId(transactionId)
            .responseBody(responseBody)
            .build();
    }

    public static PayCallbackResponse fail(String paymentNo, String errorCode, String errorMessage, String responseBody) {
        return PayCallbackResponse.builder()
            .success(false)
            .paymentNo(paymentNo)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .responseBody(responseBody)
            .build();
    }
}