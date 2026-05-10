package com.lobster.trade.payment.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.lobster.trade.payment.config.AlipayProperties;
import com.lobster.trade.payment.entity.*;
import com.lobster.trade.payment.service.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付宝支付网关实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayGateway implements PaymentGateway {

    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;

    @Override
    public PayChannel getChannel() {
        return PayChannel.ALIPAY;
    }

    @Override
    public boolean isEnabled() {
        return alipayProperties.isEnabled()
            && alipayProperties.getAppId() != null
            && !alipayProperties.getAppId().isEmpty()
            && alipayProperties.getPrivateKey() != null
            && !alipayProperties.getPrivateKey().isEmpty()
            && alipayProperties.getAlipayPublicKey() != null
            && !alipayProperties.getAlipayPublicKey().isEmpty();
    }

    @Override
    public PayResponse createPayment(PayRequest request) {
        if (!isEnabled()) {
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .channel(PayChannel.ALIPAY)
                .status(PayStatus.FAIL)
                .errorMessage("支付宝未启用")
                .build();
        }

        try {
            AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
            // 异步回调（关键！）
            payRequest.setNotifyUrl(alipayProperties.getNotifyUrl());
            // 前端回跳
            if (request.getReturnUrl() != null && !request.getReturnUrl().isEmpty()) {
                payRequest.setReturnUrl(request.getReturnUrl());
            }

            // 构建业务参数
            String subject = buildSubject(request);
            String bizContent = "{"
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\","
                + "\"out_trade_no\":\"" + request.getPaymentNo() + "\","
                + "\"total_amount\":\"" + request.getAmount().toPlainString() + "\","
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"body\":\"" + escapeJson(request.getDescription() != null ? request.getDescription() : "龙虾平台支付") + "\""
                + "}";
            payRequest.setBizContent(bizContent);

            AlipayTradePagePayResponse response = alipayClient.pageExecute(payRequest);
            if (response.isSuccess()) {
                log.info("[ALIPAY_GATEWAY] 支付创建成功: paymentNo={}, amount={}", request.getPaymentNo(), request.getAmount());
                return PayResponse.builder()
                    .paymentNo(request.getPaymentNo())
                    .channel(PayChannel.ALIPAY)
                    .status(PayStatus.PENDING)
                    .amount(request.getAmount())
                    .htmlForm(response.getBody())
                    .expireTime(LocalDateTime.now().plusMinutes(10))
                    .build();
            } else {
                log.error("[ALIPAY_GATEWAY] 支付创建失败: {}, {}", request.getPaymentNo(), response.getMsg());
                return PayResponse.builder()
                    .paymentNo(request.getPaymentNo())
                    .channel(PayChannel.ALIPAY)
                    .status(PayStatus.FAIL)
                    .errorMessage("支付宝下单失败: " + response.getMsg())
                    .build();
            }
        } catch (Exception e) {
            log.error("[ALIPAY_GATEWAY] 支付异常: paymentNo={}", request.getPaymentNo(), e);
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .channel(PayChannel.ALIPAY)
                .status(PayStatus.FAIL)
                .errorMessage("支付宝异常: " + e.getMessage())
                .build();
        }
    }

    @Override
    public boolean verifyCallback(PayCallbackRequest callback) {
        if (callback.getAllParams() == null || callback.getAllParams().isEmpty()) {
            log.warn("[ALIPAY_GATEWAY] 回调参数为空");
            return false;
        }
        try {
            // 使用 AlipaySignature.rsaCheckV2 验签（支付宝官方方法）
            // 参数需包含sign和sign_type，验签时自动排除
            boolean result = AlipaySignature.rsaCheckV2(
                callback.getAllParams(),
                alipayProperties.getAlipayPublicKey(),
                "UTF-8",
                "RSA2"
            );
            if (!result) {
                log.warn("[ALIPAY_GATEWAY] 支付宝签名验证失败: paymentNo={}", callback.getPaymentNo());
            }
            return result;
        } catch (Exception e) {
            log.error("[ALIPAY_GATEWAY] 验签异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public PayCallbackResponse handleCallback(PayCallbackRequest callback) {
        String tradeStatus = callback.getTradeStatus();
        String outTradeNo = callback.getPaymentNo();
        String tradeNo = callback.getTransactionId();
        String totalAmount = callback.getTotalAmount();

        log.info("[ALIPAY_GATEWAY] 回调处理: outTradeNo={}, tradeStatus={}, tradeNo={}, amount={}",
            outTradeNo, tradeStatus, tradeNo, totalAmount);

        // 只有 TRADE_SUCCESS / TRADE_FINISHED 才算成功
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            return PayCallbackResponse.success(outTradeNo, tradeNo, "success");
        } else {
            return PayCallbackResponse.fail(outTradeNo, tradeStatus, "支付未成功: " + tradeStatus, "fail");
        }
    }

    @Override
    public String queryTrade(String paymentNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + paymentNo + "\"}");
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess() && response.getTradeStatus() != null) {
                return response.getTradeStatus();
            }
        } catch (Exception e) {
            log.error("[ALIPAY_GATEWAY] 查询失败: paymentNo={}", paymentNo, e);
        }
        return null;
    }

    private String buildSubject(PayRequest request) {
        if (request.getType() == PayType.RECHARGE) {
            return "龙虾平台余额充值-" + request.getPaymentNo();
        } else if (request.getType() == PayType.ORDER) {
            return "龙虾平台订单支付-" + request.getPaymentNo();
        }
        return "龙虾平台支付-" + request.getPaymentNo();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}