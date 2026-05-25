package com.lobster.trade.service;

import java.util.Map;

/**
 * 支付配置服务（管理数据库中的支付参数）
 * 与 PlatformConfigService 的区别：
 * - PlatformConfigService：通用键值存储
 * - PaymentConfigService：专用于支付渠道的运行时配置，含刷新机制
 */
public interface PaymentConfigService {

    /**
     * 获取所有支付配置（从数据库）
     */
    Map<String, String> getPaymentConfig();

    /**
     * 保存支付配置（写入数据库 + 通知 Spring 刷新）
     */
    void savePaymentConfig(Map<String, String> config);

    /**
     * 获取单个配置项（直接从数据库）
     */
    String getValue(String key);

    /**
     * 强制刷新所有支付配置 Bean（通知 Spring 重新注入）
     * 用于：保存配置后，手动触发 @RefreshScope bean 重新加载
     */
    void refreshPaymentConfigs();

    // ---- 支付宝配置键名常量 ----
    String ALIPAY_APP_ID = "alipay_app_id";
    String ALIPAY_PRIVATE_KEY = "alipay_private_key";
    String ALIPAY_PUBLIC_KEY = "alipay_public_key";
    String ALIPAY_NOTIFY_URL = "alipay_notify_url";
    String ALIPAY_RETURN_URL = "alipay_return_url";
    String ALIPAY_ENABLED = "alipay_enabled";
    String ALIPAY_SANDBOX = "alipay_sandbox";

    // ---- 微信支付配置键名常量 ----
    String WECHAT_APP_ID = "wechat_app_id";
    String WECHAT_MCH_ID = "wechat_mch_id";
    String WECHAT_API_V3_KEY = "wechat_api_v3_key";
    String WECHAT_API_V2_KEY = "wechat_api_v2_key";
    String WECHAT_CERT_SERIAL_NO = "wechat_cert_serial_no";
    String WECHAT_CERT_CONTENT = "wechat_cert_content";
    String WECHAT_NOTIFY_URL = "wechat_notify_url";
    String WECHAT_ENABLED = "wechat_enabled";
}