package com.lobster.trade.payment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付签名/验签工具（通用）
 * 支持：支付宝RSA2、微信支付APIv3、银行卡聚合支付
 */
@Component
public class PaymentSignatureUtil {

    private static final Logger log = LoggerFactory.getLogger(PaymentSignatureUtil.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    // ========== 内部签名（平台防伪）==========

    /**
     * 生成内部签名（HMAC-SHA256）
     * 用于：Mock回调防伪、内部防篡改
     * @param data 需要签名的数据
     * @param secret 签名密钥（来自 payment.secret）
     * @return Base64签名
     */
    public static String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }

    /**
     * 验证内部签名
     */
    public static boolean verifySign(String data, String signature, String secret) {
        if (data == null || signature == null || secret == null) {
            return false;
        }
        String expected = sign(data, secret);
        return expected.equals(signature);
    }

    // ========== 支付宝RSA2验签==========

    /**
     * 验证支付宝回调签名（RSA2）
     * @param params 支付宝回调参数（包含sign字段）
     * @param alipayPublicKey 支付宝公钥
     * @return true=签名有效
     */
    public static boolean verifyAlipaySign(Map<String, String> params, String alipayPublicKey) {
        if (params == null || params.isEmpty()) {
            return false;
        }
        try {
            // 支付宝签名参数不含sign和sign_type，需排除后按字典序拼接
            Map<String, String> sortedParams = new TreeMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                // 排除 sign 和 sign_type
                if ("sign".equals(key) || "sign_type".equals(key) || key == null || value == null || value.isEmpty()) {
                    continue;
                }
                sortedParams.put(key, value);
            }

            StringBuilder signData = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (!first) signData.append("&");
                first = false;
                signData.append(entry.getKey()).append("=").append(entry.getValue());
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            byte[] keyBytes = Base64.getDecoder().decode(alipayPublicKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            sig.initVerify(kf.generatePublic(keySpec));
            sig.update(signData.toString().getBytes(StandardCharsets.UTF_8));

            String sign = params.get("sign");
            if (sign == null || sign.isEmpty()) {
                log.warn("[ALIPAY_SIGN] 缺少sign字段");
                return false;
            }

            boolean result = sig.verify(Base64.getDecoder().decode(sign));
            if (!result) {
                log.warn("[ALIPAY_SIGN] 签名验证失败, signData={}", signData);
            }
            return result;
        } catch (Exception e) {
            log.error("[ALIPAY_SIGN] 验签异常: {}", e.getMessage());
            return false;
        }
    }

    // ========== 微信支付APIv3验签==========

    /**
     * 验证微信支付回调签名（APIv3 RSA）
     * @param signedData 待验证数据（HTTP body）
     * @param signature 微信返回的签名（base64）
     * @param serialNo 平台证书序列号
     * @param certContent 平台证书内容（PEM格式）
     * @return true=签名有效
     */
    public static boolean verifyWechatSign(String signedData, String signature, String serialNo, String certContent) {
        if (signedData == null || signature == null || certContent == null) {
            log.warn("[WECHAT_SIGN] 参数缺失");
            return false;
        }
        try {
            // 从证书提取公钥
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(
                new java.io.ByteArrayInputStream(certContent.getBytes(StandardCharsets.UTF_8)));

            // 验证序列号
            String actualSerial = cert.getSerialNumber().toString(16).toUpperCase();
            if (serialNo != null && !serialNo.equalsIgnoreCase(actualSerial)) {
                log.warn("[WECHAT_SIGN] 证书序列号不匹配: expected={}, actual={}", serialNo, actualSerial);
                return false;
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(cert.getPublicKey());
            sig.update(signedData.getBytes(StandardCharsets.UTF_8));

            boolean result = sig.verify(Base64.getDecoder().decode(signature));
            if (!result) {
                log.warn("[WECHAT_SIGN] 签名验证失败");
            }
            return result;
        } catch (Exception e) {
            log.error("[WECHAT_SIGN] 验签异常: {}", e.getMessage());
            return false;
        }
    }

    // ========== 银行卡/聚合支付签名==========

    /**
     * 验证银行卡/聚合支付签名（MD5/HMAC-SHA256）
     * @param params 回调参数
     * @param signKey 签名密钥
     * @param signField 签名字段名
     * @return true=签名有效
     */
    public static boolean verifyBankCardSign(Map<String, String> params, String signKey, String signField) {
        if (params == null || signKey == null) {
            return false;
        }
        // 排除签名字段本身，按字典序拼接value后MD5
        StringBuilder sb = new StringBuilder();
        new TreeMap<>(params).forEach((k, v) -> {
            if (signField != null && signField.equals(k)) return;
            if (v != null && !v.isEmpty()) {
                sb.append(v);
            }
        });
        sb.append(signKey);
        String expect = md5(sb.toString()).toUpperCase();
        String actual = (params.get(signField) == null ? "" : params.get(signField)).toUpperCase();
        return expect.equals(actual);
    }

    private static String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5计算失败", e);
        }
    }

    /**
     * 生成加盐MD5（用于内部防伪）
     */
    public static String signWithSalt(String data, String secret) {
        return md5(data + secret);
    }

    /**
     * 生成时间戳token（一次性）
     */
    public static String generateToken(String paymentNo, String secret, long expireMs) {
        long expire = System.currentTimeMillis() + expireMs;
        String raw = paymentNo + "|" + expire + "|" + secret;
        return expire + "." + md5(raw);
    }
}