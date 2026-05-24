package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.PlatformConfigMapper;
import com.lobster.trade.model.entity.PlatformConfig;
import com.lobster.trade.service.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlatformConfigServiceImpl implements PlatformConfigService {

    private final PlatformConfigMapper platformConfigMapper;

    private static final String ALIPAY_APP_ID = "alipay_app_id";
    private static final String ALIPAY_PRIVATE_KEY = "alipay_private_key";
    private static final String ALIPAY_PUBLIC_KEY = "alipay_public_key";
    private static final String ALIPAY_NOTIFY_URL = "alipay_notify_url";
    private static final String ALIPAY_RETURN_URL = "alipay_return_url";
    private static final String ALIPAY_ENABLED = "alipay_enabled";
    private static final String ALIPAY_SANDBOX = "alipay_sandbox";

    private static final String WECHAT_APP_ID = "wechat_app_id";
    private static final String WECHAT_MCH_ID = "wechat_mch_id";
    private static final String WECHAT_API_V3_KEY = "wechat_api_v3_key";
    private static final String WECHAT_API_V2_KEY = "wechat_api_v2_key";
    private static final String WECHAT_CERT_SERIAL_NO = "wechat_cert_serial_no";
    private static final String WECHAT_CERT_CONTENT = "wechat_cert_content";
    private static final String WECHAT_NOTIFY_URL = "wechat_notify_url";
    private static final String WECHAT_ENABLED = "wechat_enabled";

    // 阿里云短信配置 Key 常量
    private static final String ALIYUN_SMS_ACCESS_KEY_ID = "aliyun_sms_access_key_id";
    private static final String ALIYUN_SMS_ACCESS_KEY_SECRET = "aliyun_sms_access_key_secret";
    private static final String ALIYUN_SMS_SIGN_NAME = "aliyun_sms_sign_name";
    private static final String ALIYUN_SMS_TEMPLATE_CODE = "aliyun_sms_template_code";
    private static final String ALIYUN_SMS_TEMPLATE_PARAM_JSON = "aliyun_sms_template_param_json";
    private static final String ALIYUN_SMS_ENABLED = "aliyun_sms_enabled";

    @Override
    public String getValue(String key) {
        LambdaQueryWrapper<PlatformConfig> q = new LambdaQueryWrapper<>();
        q.eq(PlatformConfig::getConfigKey, key);
        PlatformConfig config = platformConfigMapper.selectOne(q);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setValue(String key, String value, String name, String desc) {
        LambdaQueryWrapper<PlatformConfig> q = new LambdaQueryWrapper<>();
        q.eq(PlatformConfig::getConfigKey, key);
        PlatformConfig config = platformConfigMapper.selectOne(q);
        if (config == null) {
            config = new PlatformConfig();
            config.setConfigKey(key);
            config.setConfigName(name);
            config.setConfigDesc(desc);
            config.setConfigValue(value);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            platformConfigMapper.insert(config);
        } else {
            config.setConfigValue(value);
            if (StringUtils.hasText(name)) config.setConfigName(name);
            if (StringUtils.hasText(desc)) config.setConfigDesc(desc);
            config.setUpdateTime(LocalDateTime.now());
            platformConfigMapper.updateById(config);
        }
    }

    @Override
    public Map<String, String> getPaymentConfig() {
        Map<String, String> map = new HashMap<>();
        map.put(ALIPAY_APP_ID, getValue(ALIPAY_APP_ID));
        map.put(ALIPAY_PRIVATE_KEY, getValue(ALIPAY_PRIVATE_KEY));
        map.put(ALIPAY_PUBLIC_KEY, getValue(ALIPAY_PUBLIC_KEY));
        map.put(ALIPAY_NOTIFY_URL, getValue(ALIPAY_NOTIFY_URL));
        map.put(ALIPAY_RETURN_URL, getValue(ALIPAY_RETURN_URL));
        map.put(ALIPAY_ENABLED, getValue(ALIPAY_ENABLED));
        map.put(ALIPAY_SANDBOX, getValue(ALIPAY_SANDBOX));
        map.put(WECHAT_APP_ID, getValue(WECHAT_APP_ID));
        map.put(WECHAT_MCH_ID, getValue(WECHAT_MCH_ID));
        map.put(WECHAT_API_V3_KEY, getValue(WECHAT_API_V3_KEY));
        map.put(WECHAT_API_V2_KEY, getValue(WECHAT_API_V2_KEY));
        map.put(WECHAT_CERT_SERIAL_NO, getValue(WECHAT_CERT_SERIAL_NO));
        map.put(WECHAT_CERT_CONTENT, getValue(WECHAT_CERT_CONTENT));
        map.put(WECHAT_NOTIFY_URL, getValue(WECHAT_NOTIFY_URL));
        map.put(WECHAT_ENABLED, getValue(WECHAT_ENABLED));
        return map;
    }

    @Override
    public Map<String, String> getSmsConfig() {
        Map<String, String> map = new HashMap<>();
        map.put(ALIYUN_SMS_ACCESS_KEY_ID, getValue(ALIYUN_SMS_ACCESS_KEY_ID));
        map.put(ALIYUN_SMS_ACCESS_KEY_SECRET, getValue(ALIYUN_SMS_ACCESS_KEY_SECRET));
        map.put(ALIYUN_SMS_SIGN_NAME, getValue(ALIYUN_SMS_SIGN_NAME));
        map.put(ALIYUN_SMS_TEMPLATE_CODE, getValue(ALIYUN_SMS_TEMPLATE_CODE));
        map.put(ALIYUN_SMS_TEMPLATE_PARAM_JSON, getValue(ALIYUN_SMS_TEMPLATE_PARAM_JSON));
        map.put(ALIYUN_SMS_ENABLED, getValue(ALIYUN_SMS_ENABLED));
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePaymentConfig(Map<String, String> config) {
        if (config == null) return;
        config.forEach((key, value) -> {
            String name = null, desc = null;
            switch (key) {
                case ALIPAY_APP_ID -> name = "支付宝AppID";
                case ALIPAY_PRIVATE_KEY -> name = "支付宝应用私钥";
                case ALIPAY_PUBLIC_KEY -> name = "支付宝公钥";
                case ALIPAY_NOTIFY_URL -> name = "支付宝异步回调地址";
                case ALIPAY_RETURN_URL -> name = "支付宝同步回调地址";
                case ALIPAY_ENABLED -> name = "支付宝启用状态";
                case ALIPAY_SANDBOX -> name = "支付宝沙箱模式";
                case WECHAT_APP_ID -> name = "微信AppID";
                case WECHAT_MCH_ID -> name = "微信商户号";
                case WECHAT_API_V3_KEY -> name = "微信APIv3密钥";
                case WECHAT_API_V2_KEY -> name = "微信APIv2密钥";
                case WECHAT_CERT_SERIAL_NO -> name = "微信证书序列号";
                case WECHAT_CERT_CONTENT -> name = "微信证书内容";
                case WECHAT_NOTIFY_URL -> name = "微信异步回调地址";
                case WECHAT_ENABLED -> name = "微信启用状态";
                case ALIYUN_SMS_ACCESS_KEY_ID -> name = "阿里云AccessKeyId";
                case ALIYUN_SMS_ACCESS_KEY_SECRET -> name = "阿里云AccessKeySecret";
                case ALIYUN_SMS_SIGN_NAME -> name = "阿里云短信签名";
                case ALIYUN_SMS_TEMPLATE_CODE -> name = "阿里云模板CODE";
                case ALIYUN_SMS_TEMPLATE_PARAM_JSON -> name = "阿里云模板参数JSON";
                case ALIYUN_SMS_ENABLED -> name = "阿里云短信启用状态";
            }
            if (name != null) {
                setValue(key, value, name, desc);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSmsConfig(Map<String, String> config) {
        if (config == null) return;
        config.forEach((key, value) -> {
            String name = null, desc = null;
            switch (key) {
                case ALIYUN_SMS_ACCESS_KEY_ID -> name = "阿里云AccessKeyId";
                case ALIYUN_SMS_ACCESS_KEY_SECRET -> name = "阿里云AccessKeySecret";
                case ALIYUN_SMS_SIGN_NAME -> name = "阿里云短信签名";
                case ALIYUN_SMS_TEMPLATE_CODE -> name = "阿里云模板CODE";
                case ALIYUN_SMS_TEMPLATE_PARAM_JSON -> name = "阿里云模板参数JSON";
                case ALIYUN_SMS_ENABLED -> name = "阿里云短信启用状态";
            }
            if (name != null) {
                setValue(key, value, name, desc);
            }
        });
    }
}