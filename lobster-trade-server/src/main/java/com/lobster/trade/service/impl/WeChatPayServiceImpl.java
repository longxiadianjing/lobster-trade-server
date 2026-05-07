package com.lobster.trade.service.impl;

import com.lobster.trade.config.WeChatConfig;
import com.lobster.trade.model.entity.PaymentTransaction;
import com.lobster.trade.service.WeChatPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatPayServiceImpl implements WeChatPayService {

    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    private static final String HTTPS_PREFIX = "https://";

    private final WeChatConfig weChatConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String createNativeOrder(PaymentTransaction transaction, String description) {
        if (!weChatConfig.isEnabled()) {
            log.warn("[WECHAT_PAY] 微信支付未启用");
            return null;
        }

        try {
            // 构建参数（按 ASCII 排序）
            Map<String, String> params = new TreeMap<>();
            params.put("appid", weChatConfig.getAppId());
            params.put("mch_id", weChatConfig.getMchId());
            params.put("nonce_str", generateNonceStr());
            params.put("body", description);
            params.put("out_trade_no", transaction.getPaymentNo());
            // 金额：元 → 分（微信支付单位是分）
            params.put("total_fee", transaction.getAmount().multiply(new java.math.BigDecimal("100")).intValue() + "");
            params.put("spbill_create_ip", "0.0.0.0");
            params.put("notify_url", weChatConfig.getNotifyUrl());
            params.put("trade_type", "NATIVE");

            // 生成签名
            String sign = buildSign(params, weChatConfig.getApiKey());
            params.put("sign", sign);

            // 发送请求
            String respXml = postXml(UNIFIED_ORDER_URL, params);
            log.info("[WECHAT_PAY] 统位下单响应: {}", respXml);

            // 解析 XML 响应
            Map<String, String> resp = parseXml(respXml);
            if ("SUCCESS".equals(resp.get("return_code")) && "SUCCESS".equals(resp.get("result_code"))) {
                return resp.get("code_url");
            } else {
                log.error("[WECHAT_PAY] 微信下单失败: return_code={}, return_msg={}, err_code={}, err_code_des={}",
                    resp.get("return_code"), resp.get("return_msg"), resp.get("err_code"), resp.get("err_code_des"));
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
        // 微信回调参数本身就是 flat 的 k=v&...
        // 返回状态码、交易单号、第三方交易号
        result.put("return_code", params.get("return_code"));
        result.put("transaction_id", params.get("transaction_id"));
        result.put("out_trade_no", params.get("out_trade_no"));
        result.put("amount", params.get("total_fee")); // 单位：分
        result.put("paid_time", params.get("time_end"));
        return result;
    }

    // ==================== 内部方法 ====================

    private String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 构建 APIv2 签名（HMAC-SHA256）
     */
    private String buildSign(Map<String, String> params, String apiKey) {
        // 拼接 stringA
        String stringA = params.entrySet().stream()
            .filter(e -> !"sign".equals(e.getKey()) && e.getValue() != null && !"".equals(e.getValue()))
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));
        String stringSignTemp = stringA + "&key=" + apiKey;
        // HMAC-SHA256 → hex → uppercase
        return HmacUtils.hmacSha256Hex(apiKey, stringSignTemp).toUpperCase();
    }

    /**
     * 发送 XML POST 请求
     */
    private String postXml(String url, Map<String, String> params) {
        String xml = buildXml(params);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/xml;charset=UTF-8"));
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(xml, headers);
        return restTemplate.postForObject(url, entity, String.class);
    }

    /**
     * 构建 XML
     */
    private String buildXml(Map<String, String> params) {
        StringBuilder sb = new StringBuilder("<xml>");
        for (Map.Entry<String, String> e : params.entrySet()) {
            sb.append("<").append(e.getKey()).append("><![CDATA[")
              .append(e.getValue() != null ? e.getValue() : "")
              .append("]]></").append(e.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 简单解析 XML（提取一级子节点文本）
     */
    private Map<String, String> parseXml(String xml) {
        Map<String, String> result = new HashMap<>();
        // 提取 <key><![CDATA[value]]></key> 格式
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("<(\\w+)><!\\[CDATA\\[([^\\]]*)\\]\\]></\\1>");
        java.util.regex.Matcher m = p.matcher(xml);
        while (m.find()) {
            result.put(m.group(1), m.group(2));
        }
        // 兼容不带 CDATA 的
        java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("<(\\w+)>([^<]*)</\\1>");
        java.util.regex.Matcher m2 = p2.matcher(xml);
        while (m2.find()) {
            if (!result.containsKey(m2.group(1))) {
                result.put(m2.group(1), m2.group(2).trim());
            }
        }
        return result;
    }
}
