package com.lobster.trade.service;

import java.util.Map;

/**
 * 短信配置服务（管理数据库中的短信参数）
 */
public interface SmsConfigService {

    /**
     * 获取所有短信配置（从数据库）
     */
    Map<String, String> getSmsConfig();

    /**
     * 保存短信配置（写入数据库）
     */
    void saveSmsConfig(Map<String, String> config);

    /**
     * 获取单个配置项
     */
    String getValue(String key);

    // ---- 短信配置键名常量 ----
    String ALIYUN_SMS_ACCESS_KEY_ID = "aliyun_sms_access_key_id";
    String ALIYUN_SMS_ACCESS_KEY_SECRET = "aliyun_sms_access_key_secret";
    String ALIYUN_SMS_SIGN_NAME = "aliyun_sms_sign_name";
    String ALIYUN_SMS_TEMPLATE_CODE = "aliyun_sms_template_code";
    String ALIYUN_SMS_TEMPLATE_PARAM_JSON = "aliyun_sms_template_param_json";
    String ALIYUN_SMS_ENABLED = "aliyun_sms_enabled";
}