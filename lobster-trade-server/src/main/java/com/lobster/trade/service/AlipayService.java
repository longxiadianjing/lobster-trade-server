package com.lobster.trade.service;

import com.lobster.trade.model.entity.PaymentTransaction;
import java.math.BigDecimal;

/**
 * 支付宝支付服务接口
 */
public interface AlipayService {

    /**
     * 充值下单（扫码支付）
     * @return 支付宝支付的form表单（用于跳转）
     */
    String createRechargePayment(Long userId, BigDecimal amount, String paymentNo);

    /**
     * 处理支付宝异步回调
     * @param params 回调参数map
     * @return success/fail
     */
    String handleNotify(java.util.Map<String, String> params);

    /**
     * 查询支付宝交易状态
     */
    String queryTrade(String paymentNo);
}