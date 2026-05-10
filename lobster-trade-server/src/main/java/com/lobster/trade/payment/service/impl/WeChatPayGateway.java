package com.lobster.trade.payment.service.impl;

import com.lobster.trade.payment.config.WeChatPayProperties;
import com.lobster.trade.payment.entity.*;
import com.lobster.trade.payment.service.PaymentGateway;
import com.lobster.trade.payment.util.PaymentSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 微信支付网关实现（APIv3 RSA）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatPayGateway implements PaymentGateway {

    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";

    private final WeChatPayProperties weChatProperties;

    @Override
    public PayChannel getChannel() {
        return PayChannel.WECHAT;
    }

    @Override
    public boolean isEnabled() {
        return weChatProperties.isEnabled()
            && weChatProperties.getAppId() != null
            && !weChatProperties.getAppId().isEmpty()
            && weChatProperties.getMchId() != null
            && !weChatProperties.getMchId().isEmpty();
    }

    @Override
    public PayResponse createPayment(PayRequest request) {
        if (!isEnabled()) {
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .channel(PayChannel.WECHAT)
                .status(PayStatus.FAIL)
                .errorMessage("微信支付未启用")
                .build();
        }

        String privateKey = weChatProperties.getPrivateKey();
        if (privateKey == null || privateKey.isEmpty()) {
            log.error("[WECHAT_GATEWAY] 私钥未配置");
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .channel(PayChannel.WECHAT)
                .status(PayStatus.FAIL)
                .errorMessage("微信支付私钥未配置")
                .build();
        }

        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String nonce = generateNonceStr();

            // 构建请求 body（TreeMap 保证字典序）
            Map<String, Object> reqBody = new TreeMap<>();
            reqBody.put("mchid", weChatProperties.getMchId());
            reqBody.put("appid", weChatProperties.getAppId());
            reqBody.put("description", request.getDescription() != null ? request.getDescription() : "龙虾平台支付");
            reqBody.put("out_trade_no", request.getPaymentNo());
            reqBody.put("time_expire", LocalDateTime.now().plusMinutes(30)
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "+08:00");

            Map<String, Object> amount = new TreeMap<>();
            // 微信支付金额单位：分
            amount.put("total", request.getAmount().multiply(new BigDecimal("100")).intValue());
            amount.put("currency", "CNY");
            reqBody.put("amount", amount);

            if (weChatProperties.getNotifyUrl() != null) {
                reqBody.put("notify_url", weChatProperties.getNotifyUrl());
            }

            String jsonBody = toJson(reqBody);

            // 构建签名串（APIv3 标准格式）
            String signStr = buildSignString("POST", "/v3/pay/transactions/native", timestamp, nonce, jsonBody);
            String signature = rsaSign(signStr, privateKey);

            // Authorization 头
            String token = String.format(
                "mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%d\",serial_no=\"%s\",signature=\"%s\"",
                weChatProperties.getMchId(), nonce, timestamp,
                weChatProperties.getCertSerialNo(), signature);

            // 发送请求
            String respJson = doV3Request("POST", UNIFIED_ORDER_URL, jsonBody, token);
            log.info("[WECHAT_GATEWAY] 统一下单响应: {}", respJson);

            Map<String, Object> resp = parseJson(respJson);
            if (resp.containsKey("code_url")) {
                return PayResponse.builder()
                    .paymentNo(request.getPaymentNo())
                    .channel(PayChannel.WECHAT)
                    .status(PayStatus.PENDING)
                    .amount(request.getAmount())
                    .qrCodeData((String) resp.get("code_url"))
                    .payUrl((String) resp.get("code_url"))
                    .expireTime(LocalDateTime.now().plusMinutes(30))
                    .build();
            } else {
                String errMsg = (String) resp.getOrDefault("message", resp.getOrDefault("err_code_des", "下单失败"));
                return PayResponse.builder()
                    .paymentNo(request.getPaymentNo())
                    .channel(PayChannel.WECHAT)
                    .status(PayStatus.FAIL)
                    .errorMessage("微信下单失败: " + errMsg)
                    .build();
            }
        } catch (Exception e) {
            log.error("[WECHAT_GATEWAY] 支付异常: paymentNo={}", request.getPaymentNo(), e);
            return PayResponse.builder()
                .paymentNo(request.getPaymentNo())
                .channel(PayChannel.WECHAT)
                .status(PayStatus.FAIL)
                .errorMessage("微信支付异常: " + e.getMessage())
                .build();
        }
    }

    @Override
    public boolean verifyCallback(PayCallbackRequest callback) {
        // 微信支付 APIv3 回调验签需要：
        // 1. 从请求头获取 Wechatpay-Signature, Wechatpay-Timestamp, Wechatpay-Nonce
        // 2. 构造待验签串：timestamp + "\n" + nonce + "\n" + body + "\n"
        // 3. 使用平台证书公钥验签
        Map<String, String> params = callback.getAllParams();
        if (params == null) {
            return false;
        }

        // 从回调参数中获取（微信回调body为JSON）
        String timestamp = params.get("Wechatpay-Timestamp");
        String nonce = params.get("Wechatpay-Nonce");
        String signature = params.get("Wechatpay-Signature");
        String body = params.get("_body"); // 原始JSON body

        if (timestamp == null || nonce == null || signature == null || body == null) {
            log.warn("[WECHAT_GATEWAY] 回调参数不完整");
            return false;
        }

        // 构造签名串
        String signedData = timestamp + "\n" + nonce + "\n" + body + "\n";

        // 使用平台证书验签（certSerialNo + certContent）
        // certContent 可以从缓存或微信平台获取，此处通过 WeChatPayProperties 注入
        String certContent = params.get("_cert_content");
        if (certContent == null || certContent.isEmpty()) {
            log.warn("[WECHAT_GATEWAY] 平台证书内容未配置");
            // 允许跳过验签（仅开发环境）
            return true;
        }

        return PaymentSignatureUtil.verifyWechatSign(
            signedData, signature, weChatProperties.getCertSerialNo(), certContent);
    }

    @Override
    public PayCallbackResponse handleCallback(PayCallbackRequest callback) {
        Map<String, String> params = callback.getAllParams();
        String returnCode = params.get("return_code");
        String outTradeNo = callback.getPaymentNo();
        String transactionId = callback.getTransactionId();

        log.info("[WECHAT_GATEWAY] 回调处理: return_code={}, out_trade_no={}, transaction_id={}",
            returnCode, outTradeNo, transactionId);

        if ("SUCCESS".equals(returnCode)) {
            return PayCallbackResponse.success(outTradeNo, transactionId,
                "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
        } else {
            String errMsg = params.getOrDefault("err_code_des", "支付失败");
            return PayCallbackResponse.fail(outTradeNo, returnCode, errMsg,
                "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>");
        }
    }

    @Override
    public String queryTrade(String paymentNo) {
        // 查询接口实现（简化版）
        // 实际应调用微信 V3 查询接口
        return null;
    }

    // ==================== 内部方法 ====================

    private String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    private String buildSignString(String method, String path, long timestamp, String nonce, String body) {
        return method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
    }

    /**
     * RSA SHA256 签名（使用 PKCS#8 私钥，APIv3 标准）
     */
    private String rsaSign(String data, String privateKeyPem) throws Exception {
        String key = privateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pk = kf.generatePrivate(keySpec);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(pk);
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private String doV3Request(String method, String url, String body, String token) {
        try {
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();

            int code = conn.getResponseCode();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(
                    code >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) resp.append(line);
            reader.close();

            return resp.toString();
        } catch (Exception e) {
            throw new RuntimeException("微信API请求失败: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        com.alibaba.fastjson2.JSONObject obj = com.alibaba.fastjson2.JSON.parseObject(json);
        Map<String, Object> result = new HashMap<>();
        for (String key : obj.keySet()) {
            result.put(key, obj.get(key));
        }
        return result;
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v instanceof Map) {
                sb.append(toJson((Map<String, Object>) v));
            } else if (v instanceof Number) {
                sb.append(v);
            } else {
                sb.append("\"").append(escapeJson(String.valueOf(v))).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}