package com.lobster.trade.util;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 阿里云短信发送工具
 * SDK: com.aliyun:dysmsapi20170525
 * API文档: https://help.aliyun.com/document_detail/419729.html
 *
 * 使用方式：在 application-dev.yml 或环境变量中配置：
 *   aliyun.sms.access-key-id:     您的AccessKey ID
 *   aliyun.sms.access-key-secret: 您的AccessKey Secret
 *   aliyun.sms.sign-name:         短信签名名称（控制台申请）
 *   aliyun.sms.template-code:    短信模板CODE（控制台申请，变量名用 ${code}）
 */
@Slf4j
@Component
public class AliyunSmsUtil {

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}")
    private String endpoint;

    private Client client;

    @PostConstruct
    public void init() {
        if (!isConfigured()) {
            log.warn("[AliyunSms] 未配置完整（缺少AccessKey或模板CODE），短信功能回退到日志打印模式");
            return;
        }
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint(endpoint);
            client = new Client(config);
            log.info("[AliyunSms] 初始化成功，签名={}，模板={}", signName, templateCode);
        } catch (Exception e) {
            log.error("[AliyunSms] 初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 发送短信验证码
     * @param phone 手机号（国内如 13812345678）
     * @param code  6位数字验证码
     * @return true=发送成功，false=发送失败或未配置
     */
    public boolean sendVerifyCode(String phone, String code) {
        log.info("[AliyunSms] 准备发送验证码 phone={} code={}", phone, code);

        if (client == null) {
            // 未配置时回退：打印日志，验证码仍存入Redis可用于测试
            log.info("[AliyunSms] 跳过真实发送（未配置），验证码={}", code);
            return true;
        }

        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            SendSmsResponse response = client.sendSms(request);

            if ("OK".equals(response.getBody().getCode())) {
                log.info("[AliyunSms] 发送成功 BizId={}", response.getBody().getBizId());
                return true;
            } else {
                log.error("[AliyunSms] 发送失败 code={} message={}",
                        response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("[AliyunSms] 发送异常 phone={}: {}", phone, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查是否已完整配置阿里云短信
     * 三个配置项都非空且模板CODE不是占位符才算已配置
     */
    public boolean isConfigured() {
        return accessKeyId != null && !accessKeyId.isEmpty()
                && accessKeySecret != null && !accessKeySecret.isEmpty()
                && templateCode != null && !templateCode.isEmpty()
                && !templateCode.equals("SMS_xxxxxxx");
    }
}