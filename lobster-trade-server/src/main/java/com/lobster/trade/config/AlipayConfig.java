package com.lobster.trade.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayConfig {

    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private boolean sandbox;

    @Bean
    public AlipayClient alipayClient() {
        String gateway = sandbox
            ? "https://openapi-sandbox.dl.alipaydev.com/gateway.do"
            : "https://openapi.alipay.com/gateway.do";

        return new DefaultAlipayClient(
            gateway,
            appId,
            privateKey,
            "json",
            "UTF-8",
            alipayPublicKey,
            "RSA2"
        );
    }
}