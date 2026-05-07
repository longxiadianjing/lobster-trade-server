package com.lobster.trade.service.impl;

import com.lobster.trade.config.WeChatConfig;
import com.lobster.trade.model.entity.PaymentTransaction;
import com.lobster.trade.service.WeChatPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatPayServiceImpl implements WeChatPayService {

    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
    private static final String PLATFORM_CERT_URL = "https://api.mch.weixin.qq.com/v3/certificates";

    private final WeChatConfig weChatConfig;

    @Override
    public String createNativeOrder(PaymentTransaction transaction, String description) {
        if (!weChatConfig.isEnabled()) {
            log.warn("[WECHAT_PAY] 微信支付未启用");
            return null;
        }
        if (weChatConfig.getPrivateKey() == null || weChatConfig.getPrivateKey().isEmpty()) {
            log.error("[WECHAT_PAY] 私钥未配置，请设置 WECHAT_PRIVATE_KEY");
            return null;
        }

        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String nonce = generateNonceStr();

            // 构建请求 body（JSON）
            Map<String, Object> reqBody = new TreeMap<>();
            reqBody.put("mchid", weChatConfig.getMchId());
            reqBody.put("appid", weChatConfig.getAppId());
            reqBody.put("description", description);
            reqBody.put("out_trade_no", transaction.getPaymentNo());
            reqBody.put("time_expire", java.time.LocalDateTime.now()
                .plusMinutes(30).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "+08:00");
            Map<String, Object> amount = new TreeMap<>();
            // 微信支付单位是分
            amount.put("total", transaction.getAmount().multiply(new java.math.BigDecimal("100")).intValue());
            amount.put("currency", "CNY");
            reqBody.put("amount", amount);
            reqBody.put("notify_url", weChatConfig.getNotifyUrl());

            String jsonBody = toJson(reqBody);

            // 构建签名串
            String signStr = buildSignString("POST", "/v3/pay/transactions/native", timestamp, nonce, jsonBody);
            String signature = rsaSign(signStr, weChatConfig.getPrivateKey());

            // 构建 Authorization 头
            String token = String.format("mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%d\",serial_no=\"%s\",signature=\"%s\"",
                weChatConfig.getMchId(), nonce, timestamp, weChatConfig.getCertSerialNo(), signature);

            // 发送请求
            String respJson = doV3Request("POST", UNIFIED_ORDER_URL, jsonBody, token);
            log.info("[WECHAT_PAY] 统位下单响应: {}", respJson);

            Map<String, Object> resp = parseJson(respJson);
            if (resp.containsKey("code_url")) {
                return (String) resp.get("code_url");
            } else {
                log.error("[WECHAT_PAY] 微信下单失败: {}", respJson);
                return null;
            }
        } catch (Exception e) {
            log.error("[WECHAT_PAY] 微信支付创建订单异常", e);
            return null;
        }
    }

    @Override
    public Map<String, String> parseNotifyResult(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();
        result.put("return_code", params.get("return_code"));
        result.put("transaction_id", params.get("transaction_id"));
        result.put("out_trade_no", params.get("out_trade_no"));
        result.put("amount", params.get("amount")); // 回调里的amount结构
        result.put("paid_time", params.get("time_end"));
        return result;
    }

    // ==================== 内部方法 ====================

    private String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 构建 APIv3 签名串
     * HTTP method + "\n" + URL path + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n"
     */
    private String buildSignString(String method, String path, long timestamp, String nonce, String body) {
        return method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
    }

    /**
     * RSA SHA256 签名（使用 PKCS#8 私钥）
     */
    private String rsaSign(String data, String privateKeyPem) throws Exception {
        // 去掉 PEM 头尾和空白
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

    /**
     * 发送 V3 API 请求（JSON）
     */
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

            if (code != 200 && code != 204) {
                log.warn("[WECHAT_PAY] HTTP {}: {}", code, resp);
            }
            return resp.toString();
        } catch (Exception e) {
            log.error("[WECHAT_PAY] 请求失败: {}", e.getMessage());
            throw new RuntimeException("微信API请求失败", e);
        }
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        com.alibaba.fastjson2.JSONObject obj = com.alibaba.fastjson2.JSON.parseObject(json);
        Map<String, Object> result = new HashMap<>();
        for (String key : obj.keySet()) {
            result.put(key, obj.get(key));
        }
        return result;
    }
}
