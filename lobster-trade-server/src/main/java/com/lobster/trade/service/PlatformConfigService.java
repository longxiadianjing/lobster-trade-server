package com.lobster.trade.service;

import java.util.Map;

public interface PlatformConfigService {

    String getValue(String key);

    void setValue(String key, String value, String name, String desc);

    Map<String, String> getPaymentConfig();

    void savePaymentConfig(Map<String, String> config);

    Map<String, String> getSmsConfig();

    void saveSmsConfig(Map<String, String> config);
}