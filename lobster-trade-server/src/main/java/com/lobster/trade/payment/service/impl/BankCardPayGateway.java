package com.lobster.trade.payment.service.impl;

import com.lobster.trade.payment.config.BankCardProperties;
import com.lobster.trade.payment.entity.*;
import com.lobster.trade.payment.service.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 银行卡支付网关（预留实现）
 * 后期接入：拉卡拉 / 易宝支付 / 银联商务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankCardPayGateway implements PaymentGateway {

    private final BankCardProperties bankCardProperties;

    @Override
    public PayChannel getChannel() {
        return PayChannel.BANK_CARD;
    }

    @Override
    public boolean isEnabled() {
        // 默认禁用，待接入后启用
        return bankCardProperties.isEnabled()
            && bankCardProperties.getMchId() != null
            && !bankCardProperties.getMchId().isEmpty();
    }

    @Override
    public PayResponse createPayment(PayRequest request) {
        return PayResponse.builder()
            .paymentNo(request.getPaymentNo())
            .channel(PayChannel.BANK_CARD)
            .status(PayStatus.FAIL)
            .errorMessage("银行卡支付正在接入中，预计下个版本支持")
            .build();
    }

    @Override
    public boolean verifyCallback(PayCallbackRequest callback) {
        // 待接入后实现
        log.warn("[BANKCARD_GATEWAY] 银行卡支付未接入，忽略回调");
        return false;
    }

    @Override
    public PayCallbackResponse handleCallback(PayCallbackRequest callback) {
        return PayCallbackResponse.fail(
            callback.getPaymentNo(),
            "NOT_IMPLEMENTED",
            "银行卡支付正在接入中",
            "fail");
    }

    @Override
    public String queryTrade(String paymentNo) {
        return null;
    }
}