package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.config.AlipayConfig;
import com.lobster.trade.config.WeChatConfig;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.PlatformConfigMapper;
import com.lobster.trade.model.entity.PlatformConfig;
import com.lobster.trade.service.PaymentConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfigServiceImpl implements PaymentConfigService {

    private final PlatformConfigMapper platformConfigMapper;
    private final AlipayConfig alipayConfig;
    private final WeChatConfig weChatConfig;

    @Override
    public String getValue(String key) {
        LambdaQueryWrapper<PlatformConfig> q = new LambdaQueryWrapper<>();
        q.eq(PlatformConfig::getConfigKey, key);
        PlatformConfig cfg = platformConfigMapper.selectOne(q);
        return cfg != null ? cfg.getConfigValue() : null;
    }

    private void setValue(String key, String value) {
        LambdaQueryWrapper<PlatformConfig> q = new LambdaQueryWrapper<>();
        q.eq(PlatformConfig::getConfigKey, key);
        PlatformConfig cfg = platformConfigMapper.selectOne(q);
        if (cfg == null) {
            cfg = new PlatformConfig();
            cfg.setConfigKey(key);
            cfg.setConfigValue(value);
            cfg.setCreateTime(LocalDateTime.now());
            cfg.setUpdateTime(LocalDateTime.now());
            platformConfigMapper.insert(cfg);
        } else {
            cfg.setConfigValue(value);
            cfg.setUpdateTime(LocalDateTime.now());
            platformConfigMapper.updateById(cfg);
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
    @Transactional(rollbackFor = Exception.class)
    public void savePaymentConfig(Map<String, String> config) {
        if (config == null) return;

        config.forEach((key, value) -> {
            switch (key) {
                case ALIPAY_APP_ID -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    alipayConfig.setAppId(value);
                }
                case ALIPAY_PRIVATE_KEY -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    alipayConfig.setPrivateKey(value);
                }
                case ALIPAY_PUBLIC_KEY -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    alipayConfig.setAlipayPublicKey(value);
                }
                case ALIPAY_NOTIFY_URL -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    alipayConfig.setNotifyUrl(value);
                }
                case ALIPAY_RETURN_URL -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    alipayConfig.setReturnUrl(value);
                }
                case ALIPAY_ENABLED -> {
                    setValue(key, value);
                    alipayConfig.setEnabled(!"false".equals(value) && !"0".equals(value));
                }
                case ALIPAY_SANDBOX -> {
                    setValue(key, value);
                    alipayConfig.setSandbox("true".equals(value) || "1".equals(value));
                }
                case WECHAT_APP_ID -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    weChatConfig.setAppId(value);
                }
                case WECHAT_MCH_ID -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    weChatConfig.setMchId(value);
                }
                case WECHAT_API_V3_KEY -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    weChatConfig.setApiKey(value);
                }
                case WECHAT_API_V2_KEY -> {
                    setValue(key, value);
                    // APIv2 key stored separately, not in WeChatConfig
                }
                case WECHAT_CERT_SERIAL_NO -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    weChatConfig.setCertSerialNo(value);
                }
                case WECHAT_CERT_CONTENT -> {
                    setValue(key, value);
                    // cert content is used in WeChatPayServiceImpl for callbacks
                }
                case WECHAT_NOTIFY_URL -> {
                    setValue(key, value);
                    if (!StringUtils.hasText(value)) return;
                    weChatConfig.setNotifyUrl(value);
                }
                case WECHAT_ENABLED -> {
                    setValue(key, value);
                    weChatConfig.setEnabled(!"false".equals(value) && !"0".equals(value));
                }
            }
        });

        log.info("[PAYMENT_CONFIG] 支付配置已更新，共 {} 项", config.size());
    }

    @Override
    public void refreshPaymentConfigs() {
        log.info("[PAYMENT_CONFIG] 刷新支付配置 Bean");
    }
}