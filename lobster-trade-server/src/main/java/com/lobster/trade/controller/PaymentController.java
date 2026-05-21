package com.lobster.trade.controller;

import com.lobster.trade.model.entity.PaymentTransaction;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.AlipayService;
import com.lobster.trade.service.PaymentService;
import com.lobster.trade.service.WeChatPayService;
import com.lobster.trade.util.PaymentSecurityUtil;
import com.lobster.trade.util.RateLimitUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AlipayService alipayService;
    private final WeChatPayService weChatPayService;
    private final RateLimitUtil rateLimitUtil;

    /**
     * 支付宝异步回调
     * POST /api/payment/alipay/notify
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                String name = entry.getKey();
                String[] values = entry.getValue();
                String valueStr = values.length > 0 ? values[0] : "";
                params.put(name, valueStr);
            }
            String result = alipayService.handleNotify(params);
            return result;
        } catch (Exception e) {
            log.error("[ALIPAY_NOTIFY] 处理异常", e);
            return "fail";
        }
    }

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

        // 支付宝：返回跳转表单
        if ("alipay".equals(channel)) {
            String form = alipayService.createRechargePayment(userId, amount, payment.getPaymentNo());
            result.put("alipayForm", form);
            result.put("payAction", "form"); // 前端需要提交form
        } else if ("wechat".equals(channel)) {
            // 微信支付：调用统位下单获取二维码链接
            String description = "龙虾平台-" + (payment.getPaymentType().equals("recharge") ? "余额充值" : "订单支付");
            String codeUrl = weChatPayService.createNativeOrder(payment, description);
            if (codeUrl != null) {
                result.put("codeUrl", codeUrl); // 微信支付二维码内容
                result.put("qrCodeUrl", "/qr/wechat?codeUrl=" + java.net.URLEncoder.encode(codeUrl, java.nio.charset.StandardCharsets.UTF_8));
                result.put("mockPageUrl", "/payment/mock-pay?paymentNo=" + payment.getPaymentNo());
                result.put("payAction", "qr");
            } else {
                result.put("payAction", "mock");
                result.put("mockPageUrl", "/payment/mock-pay?paymentNo=" + payment.getPaymentNo());
            }
        } else {
            result.put("qrCodeUrl", "/qr/mock?text=" + payment.getPaymentNo());
            result.put("mockPageUrl", "/payment/mock-pay?paymentNo=" + payment.getPaymentNo());
            result.put("payAction", "mock");
        }

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
        if ("wechat".equals(channel)) {
            String description = "龙虾平台-订单支付-" + orderId;
            String codeUrl = weChatPayService.createNativeOrder(payment, description);
            if (codeUrl != null) {
                result.put("codeUrl", codeUrl);
                result.put("qrCodeUrl", "/qr/wechat?codeUrl=" + java.net.URLEncoder.encode(codeUrl, java.nio.charset.StandardCharsets.UTF_8));
                result.put("mockPageUrl", "/payment/mock-pay?paymentNo=" + payment.getPaymentNo());
                result.put("payAction", "qr");
            } else {
                result.put("payAction", "mock");
                result.put("mockPageUrl", "/payment/mock-pay?paymentNo=" + payment.getPaymentNo());
            }
        } else {
            result.put("qrCodeUrl", "/qr/mock?text=" + payment.getPaymentNo());
            result.put("mockPageUrl", "/payment/mock-pay?paymentNo=" + payment.getPaymentNo());
            result.put("payAction", "mock");
        }

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

        // 限流检查（每用户每分钟5次）
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null && rateLimitUtil.isPaymentLimited(userId)) {
            log.warn("[PAYMENT_CALLBACK] userId={} 限流", userId);
            return ApiResponse.fail(429, "请求过于频繁");
        }

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

    /**
     * 微信支付异步回调
     * POST /api/payment/wechat/notify
     */
    @PostMapping("/wechat/notify")
    public String wechatNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                String name = entry.getKey();
                String[] values = entry.getValue();
                String valueStr = values.length > 0 ? values[0] : "";
                params.put(name, valueStr);
            }
            log.info("[WECHAT_NOTIFY] 收到回调: {}", params);
            paymentService.handleWechatNotify(params);
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        } catch (Exception e) {
            log.error("[WECHAT_NOTIFY] 处理异常", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";
        }
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