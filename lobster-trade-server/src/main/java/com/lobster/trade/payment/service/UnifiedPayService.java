package com.lobster.trade.payment.service.impl;

import com.lobster.trade.payment.config.PayChannelConfig;
import com.lobster.trade.payment.entity.*;
import com.lobster.trade.payment.service.PaymentGateway;
import com.lobster.trade.payment.util.PaymentSignatureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一支付服务（对外唯一入口）
 * 统一管理所有支付渠道，对外屏蔽各渠道差异
 */
@Slf4j
@Service
public class UnifiedPayService {

    /** 支付网关映射 */
    private final Map<PayChannel, PaymentGateway> gateways = new HashMap<>();

    /** 统一渠道配置 */
    private final PayChannelConfig payChannelConfig;

    // 通过构造函数注入所有 PaymentGateway 实现
    public UnifiedPayService(
            java.util.List<PaymentGateway> gatewayList,
            PayChannelConfig payChannelConfig) {
        for (PaymentGateway gateway : gatewayList) {
            gateways.put(gateway.getChannel(), gateway);
        }
        this.payChannelConfig = payChannelConfig;
    }

    /**
     * 对外唯一入口：发起支付
     */
    public PayResponse pay(PayRequest request) {
        // 1. 参数校验
        if (request.getUserId() == null) {
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .status(PayStatus.FAIL)
                .errorMessage("用户ID不能为空")
                .build();
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .status(PayStatus.FAIL)
                .errorMessage("金额必须大于0")
                .build();
        }
        if (request.getChannel() == null) {
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .status(PayStatus.FAIL)
                .errorMessage("支付渠道不能为空")
                .build();
        }

        // 2. 检查渠道是否启用
        PaymentGateway gateway = gateways.get(request.getChannel());
        if (gateway == null) {
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .channel(request.getChannel())
                .status(PayStatus.FAIL)
                .errorMessage("不支持的支付渠道: " + request.getChannel().getName())
                .build();
        }
        if (!gateway.isEnabled()) {
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .channel(request.getChannel())
                .status(PayStatus.FAIL)
                .errorMessage("支付渠道未启用: " + request.getChannel().getName())
                .build();
        }

        // 3. 创建支付（委托给具体网关）
        try {
            log.info("[UNIFIED_PAY] 发起支付: paymentNo={}, channel={}, amount={}, type={}",
                request.getPaymentNo(), request.getChannel(), request.getAmount(), request.getType());
            PayResponse response = gateway.createPayment(request);
            log.info("[UNIFIED_PAY] 支付响应: paymentNo={}, status={}, error={}",
                request.getPaymentNo(), response.getStatus(), response.getErrorMessage());
            return response;
        } catch (Exception e) {
            log.error("[UNIFIED_PAY] 支付异常: paymentNo={}", request.getPaymentNo(), e);
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .channel(request.getChannel())
                .status(PayStatus.FAIL)
                .errorMessage("支付异常: " + e.getMessage())
                .build();
        }
    }

    /**
     * 对外唯一入口：处理异步回调
     * @param channel 支付渠道
     * @param params 回调参数（原始 Map）
     * @return 渠道要求的标准响应（"success"/"fail" 或 XML）
     */
    public String handleNotify(PayChannel channel, Map<String, String> params) {
        PaymentGateway gateway = gateways.get(channel);
        if (gateway == null) {
            log.warn("[UNIFIED_PAY] 不支持的支付渠道: {}", channel);
            return "fail";
        }
        if (!gateway.isEnabled()) {
            log.warn("[UNIFIED_PAY] 支付渠道未启用: {}", channel);
            return "fail";
        }

        // 1. 构建回调请求
        PayCallbackRequest callback = buildCallbackRequest(channel, params);

        // 2. 验签（关键安全步骤！）
        if (!gateway.verifyCallback(callback)) {
            log.warn("[UNIFIED_PAY] 回调验签失败: channel={}, paymentNo={}", channel, callback.getPaymentNo());
            return getNotifyFailResponse(channel, "签名验证失败");
        }

        // 3. 处理回调
        PayCallbackResponse resp = gateway.handleCallback(callback);
        log.info("[UNIFIED_PAY] 回调处理完成: channel={}, paymentNo={}, success={}, response={}",
            channel, callback.getPaymentNo(), resp.isSuccess(), resp.getResponseBody());

        return resp.isSuccess() ? getNotifySuccessResponse(channel) : getNotifyFailResponse(channel, resp.getErrorMessage());
    }

    /**
     * 统一查询交易状态
     */
    public String queryTrade(String paymentNo, PayChannel channel) {
        PaymentGateway gateway = gateways.get(channel);
        if (gateway == null || !gateway.isEnabled()) {
            return null;
        }
        return gateway.queryTrade(paymentNo);
    }

    // ==================== 内部方法 ====================

    /**
     * 构建统一回调请求
     */
    private PayCallbackRequest buildCallbackRequest(PayChannel channel, Map<String, String> params) {
        PayCallbackRequest callback = new PayCallbackRequest();
        callback.setChannel(channel);
        callback.setAllParams(params);

        // 从 params 中提取关键字段（各渠道格式不同，差异化处理）
        switch (channel) {
            case ALIPAY:
                callback.setPaymentNo(params.get("out_trade_no"));
                callback.setTransactionId(params.get("trade_no"));
                callback.setTradeStatus(params.get("trade_status"));
                callback.setTotalAmount(params.get("total_amount"));
                callback.setPaidTime(params.get("gmt_payment"));
                callback.setSignature(params.get("sign"));
                break;
            case WECHAT:
                callback.setPaymentNo(params.get("out_trade_no"));
                callback.setTransactionId(params.get("transaction_id"));
                callback.setTradeStatus(params.get("trade_status"));
                callback.setTotalAmount(params.get("total_fee")); // 单位：分
                callback.setPaidTime(params.get("time_end"));
                // 从请求头获取验签数据（Wechatpay-Signature 等）
                callback.setSignature(params.get("Wechatpay-Signature"));
                break;
            case BANK_CARD:
            case UNION_PAY:
                callback.setPaymentNo(params.get("order_no"));
                callback.setTransactionId(params.get("transaction_id"));
                callback.setTradeStatus(params.get("trade_status"));
                callback.setTotalAmount(params.get("amount"));
                callback.setSignature(params.get("sign"));
                break;
            case MOCK:
                callback.setPaymentNo(params.get("payment_no"));
                callback.setTradeStatus("TRADE_SUCCESS");
                callback.setSignature(params.get("X-Payment-Signature"));
                break;
        }

        return callback;
    }

    /**
     * 获取成功响应（各渠道格式不同）
     */
    private String getNotifySuccessResponse(PayChannel channel) {
        switch (channel) {
            case ALIPAY:
                return "success";
            case WECHAT:
                return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            case BANK_CARD:
            case UNION_PAY:
                return "00"; // 银行卡成功码
            case MOCK:
                return "success";
            default:
                return "success";
        }
    }

    /**
     * 获取失败响应（各渠道格式不同）
     */
    private String getNotifyFailResponse(PayChannel channel, String reason) {
        switch (channel) {
            case ALIPAY:
                return "fail";
            case WECHAT:
                return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + (reason != null ? reason : "FAIL") + "]]></return_msg></xml>";
            case BANK_CARD:
            case UNION_PAY:
                return "01"; // 银行卡失败码
            case MOCK:
                return "fail";
            default:
                return "fail";
        }
    }

    /**
     * 获取所有已注册的网关
     */
    public Map<PayChannel, PaymentGateway> getGateways() {
        return gateways;
    }
}