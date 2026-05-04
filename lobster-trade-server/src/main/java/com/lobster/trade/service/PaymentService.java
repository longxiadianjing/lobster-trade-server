package com.lobster.trade.service;

import com.lobster.trade.model.entity.PaymentTransaction;

public interface PaymentService {

    /**
     * 创建充值支付单
     */
    PaymentTransaction createRechargePayment(Long userId, java.math.BigDecimal amount, String channel);

    /**
     * 创建订单支付单
     */
    PaymentTransaction createOrderPayment(Long userId, Long orderId, String channel);

    /**
     * 查询支付状态（用户轮询用）
     */
    PaymentTransaction getPaymentStatus(Long userId, String paymentNo);

    /**
     * Mock回调处理（模拟支付网关回调）
     */
    void processMockCallback(String paymentNo);

    /**
     * 根据paymentNo查询
     */
    PaymentTransaction getByPaymentNo(String paymentNo);
}