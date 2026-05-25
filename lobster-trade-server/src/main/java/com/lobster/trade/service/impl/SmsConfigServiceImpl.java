package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.PlatformConfigMapper;
import com.lobster.trade.model.entity.PlatformConfig;
import com.lobster.trade.service.SmsConfigService;
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
public class SmsConfigServiceImpl implements SmsConfigService {

    private final PlatformConfigMapper platformConfigMapper;

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
    public void saveSmsConfig(Map<String, String> config) {
        if (config == null) return;

        config.forEach((key, value) -> {
            switch (key) {
                case ALIYUN_SMS_ACCESS_KEY_ID -> setValue(key, value);
                case ALIYUN_SMS_ACCESS_KEY_SECRET -> setValue(key, value);
                case ALIYUN_SMS_SIGN_NAME -> setValue(key, value);
                case ALIYUN_SMS_TEMPLATE_CODE -> setValue(key, value);
                case ALIYUN_SMS_TEMPLATE_PARAM_JSON -> setValue(key, value);
                case ALIYUN_SMS_ENABLED -> setValue(key, value);
            }
        });

        log.info("[SMS_CONFIG] 短信配置已更新，共 {} 项", config.size());
    }
}