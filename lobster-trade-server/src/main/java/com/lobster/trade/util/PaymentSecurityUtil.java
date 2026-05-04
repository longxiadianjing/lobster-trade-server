package com.lobster.trade.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 支付安全工具：签名生成与验证
 */
@Component
public class PaymentSecurityUtil {

    // 从配置文件读取支付密钥
    @Value("${payment.secret:mock-payment-secret-key-2026}")
    private String paymentSecret;

    private static String STATIC_SECRET;

    @jakarta.annotation.PostConstruct
    public void init() {
        STATIC_SECRET = paymentSecret;
    }

    /**
     * 生成 HMAC-SHA256 签名
     * @param data 需要签名的数据（如 paymentNo + amount）
     * @return 签名字符串
     */
    public static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(STATIC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }

    /**
     * 验证签名
     * @param data      原始数据
     * @param signature 待验证签名
     * @return true = 签名正确，false = 签名错误
     */
    public static boolean verify(String data, String signature) {
        if (data == null || signature == null) {
            return false;
        }
        String expected = sign(data);
        return expected.equals(signature);
    }

    /**
     * 验证支付回调签名（标准参数格式）
     * 签名内容 = paymentNo + "|" + amount + "|" + status
     */
    public static boolean verifyCallback(String paymentNo, String amount, String status, String signature) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        String data = paymentNo + "|" + amount + "|" + status;
        return verify(data, signature);
    }
}
