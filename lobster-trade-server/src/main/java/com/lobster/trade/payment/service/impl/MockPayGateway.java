package com.lobster.trade.payment.service.impl;

import com.lobster.trade.payment.entity.*;
import com.lobster.trade.payment.service.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 模拟支付网关（开发测试用）
 */
@Slf4j
@Service
public class MockPayGateway implements PaymentGateway {

    @Override
    public PayChannel getChannel() {
        return PayChannel.MOCK;
    }

    @Override
    public boolean isEnabled() {
        return true; // Mock始终可用
    }

    @Override
    public PayResponse createPayment(PayRequest request) {
        log.info("[MOCK_GATEWAY] 创建模拟支付: paymentNo={}, amount={}", request.getPaymentNo(), request.getAmount());
        return PayResponse.builder()
            .paymentNo(request.getPaymentNo())
            .channel(PayChannel.MOCK)
            .status(PayStatus.PENDING)
            .amount(request.getAmount())
            .qrCodeData("MOCK_QR_" + request.getPaymentNo())
            .payUrl("/payment/mock-pay?paymentNo=" + request.getPaymentNo())
            .expireTime(LocalDateTime.now().plusMinutes(10))
            .build();
    }

    @Override
    public boolean verifyCallback(PayCallbackRequest callback) {
        // Mock不验签，接受所有回调
        return true;
    }

    @Override
    public PayCallbackResponse handleCallback(PayCallbackRequest callback) {
        return PayCallbackResponse.success(callback.getPaymentNo(), "MOCK_" + callback.getPaymentNo(), "success");
    }

    @Override
    public String queryTrade(String paymentNo) {
        return "TRADE_SUCCESS";
    }
}