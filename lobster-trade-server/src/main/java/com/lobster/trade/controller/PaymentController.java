package com.lobster.trade.controller;

import com.lobster.trade.model.entity.PaymentTransaction;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.PaymentService;
import com.lobster.trade.util.PaymentSecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建充值支付单
     * POST /api/payment/create-recharge
     */
    @PostMapping("/create-recharge")
    public ApiResponse<Map<String, Object>> createRechargePayment(
            HttpServletRequest request,
            @RequestParam BigDecimal amount,
            @RequestParam String channel) {
        Long userId = (Long) request.getAttribute("userId");
        PaymentTransaction payment = paymentService.createRechargePayment(userId, amount, channel);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("paymentNo", payment.getPaymentNo());
        result.put("amount", payment.getAmount());
        result.put("channel", payment.getChannel());
        result.put("expireTime", payment.getExpireTime());
        // Mock二维码URL（实际生产应该是真实二维码）
        result.put("qrCodeUrl", "/qr/mock?text=" + payment.getPaymentNo());
        result.put("mockPageUrl", "/payment/mock-pay?paymentNo=" + payment.getPaymentNo());

        return ApiResponse.success(result);
    }

    /**
     * 创建订单支付单
     * POST /api/payment/create-order-pay
     */
    @PostMapping("/create-order-pay")
    public ApiResponse<Map<String, Object>> createOrderPayment(
            HttpServletRequest request,
            @RequestParam Long orderId,
            @RequestParam String channel) {
        Long userId = (Long) request.getAttribute("userId");
        PaymentTransaction payment = paymentService.createOrderPayment(userId, orderId, channel);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("paymentNo", payment.getPaymentNo());
        result.put("amount", payment.getAmount());
        result.put("channel", payment.getChannel());
        result.put("expireTime", payment.getExpireTime());
        result.put("qrCodeUrl", "/qr/mock?text=" + payment.getPaymentNo());
        result.put("mockPageUrl", "/payment/mock-pay?paymentNo=" + payment.getPaymentNo());

        return ApiResponse.success(result);
    }

    /**
     * 查询支付状态（前端轮询）
     * GET /api/payment/status/{paymentNo}
     */
    @GetMapping("/status/{paymentNo}")
    public ApiResponse<Map<String, Object>> getPaymentStatus(
            HttpServletRequest request,
            @PathVariable String paymentNo) {
        Long userId = (Long) request.getAttribute("userId");
        PaymentTransaction payment = paymentService.getPaymentStatus(userId, paymentNo);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", payment.getPaymentNo());
        result.put("status", payment.getStatus());
        result.put("amount", payment.getAmount());
        result.put("channel", payment.getChannel());
        result.put("paidTime", payment.getPaidTime());
        result.put("transactionId", payment.getTransactionId());

        return ApiResponse.success(result);
    }

    /**
     * 模拟回调接口（内部使用，前端模拟支付成功）
     * POST /api/payment/mock-callback
     * 
     * 签名验证：X-Payment-Signature header
     * 签名内容 = paymentNo + "|" + amount + "|" + status
     * 本地请求（127.0.0.1/localhost）跳过签名验证
     */
    @PostMapping("/mock-callback")
    public ApiResponse<Void> mockPaymentCallback(
            HttpServletRequest request,
            @RequestParam String paymentNo) {
        
        // 签名验证（防止外部恶意调用）
        if (!isLocalRequest(request)) {
            String signature = request.getHeader("X-Payment-Signature");
            if (signature == null || signature.isEmpty()) {
                log.warn("[PAYMENT_CALLBACK] 缺少签名 paymentNo={}", paymentNo);
                return ApiResponse.fail(400, "缺少签名");
            }
            // 从数据库获取 payment 记录，用于验证签名
            PaymentTransaction payment = paymentService.getByPaymentNo(paymentNo);
            if (payment == null) {
                return ApiResponse.fail(404, "支付单不存在");
            }
            String signData = paymentNo + "|" + payment.getAmount().toPlainString() + "|" + payment.getStatus();
            if (!PaymentSecurityUtil.verify(signData, signature)) {
                log.warn("[PAYMENT_CALLBACK] 签名验证失败 paymentNo={}", paymentNo);
                return ApiResponse.fail(401, "签名验证失败");
            }
        }
        
        paymentService.processMockCallback(paymentNo);
        return ApiResponse.success(null);
    }
    
    /**
     * 判断是否为本地请求（开发调试用）
     */
    private boolean isLocalRequest(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * 获取支付结果页（Mock HTML，用于展示）
     * GET /api/payment/mock-pay-page
     */
    @GetMapping("/mock-pay-page")
    public ApiResponse<Map<String, Object>> getMockPayPage(@RequestParam String paymentNo) {
        PaymentTransaction payment = paymentService.getByPaymentNo(paymentNo);
        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", paymentNo);
        result.put("amount", payment.getAmount());
        result.put("channelName", getChannelName(payment.getChannel()));
        result.put("status", payment.getStatus());
        return ApiResponse.success(result);
    }

    private String getChannelName(String channel) {
        switch (channel) {
            case "alipay": return "支付宝";
            case "wechat": return "微信支付";
            case "bankcard": return "银行卡";
            default: return channel;
        }
    }
}