package com.lobster.trade.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.config.AlipayConfig;
import com.lobster.trade.mapper.PaymentTransactionMapper;
import com.lobster.trade.model.entity.PaymentTransaction;
import com.lobster.trade.service.AlipayService;
import com.lobster.trade.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayServiceImpl implements AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final PaymentTransactionMapper paymentMapper;
    private final WalletService walletService;

    @Override
    public String createRechargePayment(Long userId, BigDecimal amount, String paymentNo) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 优先使用数据库配置的回跳地址，未配置时使用默认值
        String returnUrl = alipayConfig.getReturnUrl();
        if (returnUrl == null || returnUrl.isEmpty()) {
            returnUrl = "http://localhost:5174/payment/result?paymentNo=" + paymentNo;
        } else {
            returnUrl += (returnUrl.contains("?") ? "&" : "?") + "paymentNo=" + paymentNo;
        }
        request.setReturnUrl(returnUrl);

        String notifyUrl = alipayConfig.getNotifyUrl();
        if (notifyUrl == null || notifyUrl.isEmpty()) {
            notifyUrl = "http://localhost:8080/api/payment/alipay/notify";
        }
        request.setNotifyUrl(notifyUrl);

        // 构建业务参数
        String bizContent = "{"
            + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\","
            + "\"out_trade_no\":\"" + paymentNo + "\","
            + "\"total_amount\":\"" + amount.toPlainString() + "\","
            + "\"subject\":\"龙虾平台充值-" + paymentNo + "\","
            + "\"body\":\"龙虾道具交易平台账户充值\""
            + "}";
        request.setBizContent(bizContent);

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                log.info("[ALIPAY] 充值下单成功: paymentNo={}, amount={}", paymentNo, amount);
                return response.getBody(); // 返回HTML表单
            } else {
                log.error("[ALIPAY] 充值下单失败: {}, {}", paymentNo, response.getMsg());
                throw new RuntimeException("支付宝下单失败: " + response.getMsg());
            }
        } catch (Exception e) {
            log.error("[ALIPAY] 充值下单异常: paymentNo={}", paymentNo, e);
            throw new RuntimeException("支付宝下单异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String handleNotify(Map<String, String> params) {
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String totalAmount = params.get("total_amount");

        log.info("[ALIPAY_NOTIFY] 回调通知: outTradeNo={}, tradeStatus={}, tradeNo={}, amount={}",
            outTradeNo, tradeStatus, tradeNo, totalAmount);

        // 【安全修复1】必须先验证签名，防止伪造回调
        if (alipayConfig.getAlipayPublicKey() != null
                && !alipayConfig.getAlipayPublicKey().isEmpty()) {
            try {
                boolean signValid = AlipaySignature.rsaCheckV2(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
                );
                if (!signValid) {
                    log.error("[ALIPAY_NOTIFY] 签名验证失败，拒绝处理: outTradeNo={}", outTradeNo);
                    return "fail";
                }
                log.info("[ALIPAY_NOTIFY] 签名验证通过: outTradeNo={}", outTradeNo);
            } catch (Exception e) {
                log.error("[ALIPAY_NOTIFY] 签名验证异常: outTradeNo={}, error={}", outTradeNo, e.getMessage());
                return "fail";
            }
        } else {
            log.warn("[ALIPAY_NOTIFY] 未配置支付宝公钥，跳过验签（测试模式）: outTradeNo={}", outTradeNo);
        }

        // 查询支付单
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getPaymentNo, outTradeNo);
        PaymentTransaction payment = paymentMapper.selectOne(wrapper);

        if (payment == null) {
            log.warn("[ALIPAY_NOTIFY] 支付单不存在: {}", outTradeNo);
            return "fail";
        }

        // 【安全修复2】金额校验，防止篡改
        if (totalAmount != null && !totalAmount.isEmpty()) {
            BigDecimal paidAmount = new BigDecimal(totalAmount);
            if (paidAmount.compareTo(payment.getAmount()) != 0) {
                log.error("[ALIPAY_NOTIFY] 金额篡改攻击！支付单={}, 系统金额={}, 回调金额={}",
                    outTradeNo, payment.getAmount(), paidAmount);
                return "fail";
            }
        }

        if (payment.getStatus() == PaymentTransaction.STATUS_SUCCESS) {
            return "success"; // 已处理，跳过（幂等性）
        }

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            payment.setStatus(PaymentTransaction.STATUS_SUCCESS);
            payment.setTransactionId(tradeNo);
            payment.setPaidTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            paymentMapper.updateById(payment);

            // 充值到账
            if (PaymentTransaction.TYPE_RECHARGE.equals(payment.getPaymentType())) {
                walletService.rechargeMock(payment.getUserId(), new BigDecimal(totalAmount), tradeNo);
            }
            log.info("[ALIPAY_NOTIFY] 支付成功: paymentNo={}, transactionId={}", outTradeNo, tradeNo);
        }

        return "success";
    }

    @Override
    public String queryTrade(String paymentNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + paymentNo + "\"}");
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                return response.getTradeStatus();
            }
        } catch (Exception e) {
            log.error("[ALIPAY] 查询失败: paymentNo={}", paymentNo, e);
        }
        return null;
    }
}