package com.lobster.trade.payment.service;

import com.lobster.trade.payment.entity.PayCallbackRequest;
import com.lobster.trade.payment.entity.PayCallbackResponse;
import com.lobster.trade.payment.entity.PayChannel;
import com.lobster.trade.payment.entity.PayRequest;
import com.lobster.trade.payment.entity.PayResponse;

/**
 * 支付网关接口（核心抽象）
 * 每个支付渠道实现对应的网关
 */
public interface PaymentGateway {

    /**
     * 获取支持的支付渠道
     */
    PayChannel getChannel();

    /**
     * 检查渠道是否启用
     */
    boolean isEnabled();

    /**
     * 创建支付
     * @param request 支付请求
     * @return 支付响应（包含跳转链接/二维码/表单）
     */
    PayResponse createPayment(PayRequest request);

    /**
     * 验证回调签名
     * @param callback 回调请求
     * @return true=签名有效，false=签名无效
     */
    boolean verifyCallback(PayCallbackRequest callback);

    /**
     * 处理回调
     * @param callback 回调请求
     * @return 回调响应
     */
    PayCallbackResponse handleCallback(PayCallbackRequest callback);

    /**
     * 查询交易状态
     * @param paymentNo 平台支付单号
     * @return 交易状态字符串（如 TRADE_SUCCESS）
     */
    String queryTrade(String paymentNo);
}