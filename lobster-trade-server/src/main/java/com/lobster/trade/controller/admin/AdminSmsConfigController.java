package com.lobster.trade.controller.admin;

import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.service.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sms-config")
@RequiredArgsConstructor
public class AdminSmsConfigController {

    private final PlatformConfigService platformConfigService;

    /** 脱敏密钥：只显示 *** + 末尾4位 */
    private String maskSecret(String value) {
        if (value == null || value.isEmpty()) return null;
        if (value.length() <= 4) return "***";
        return "***" + value.substring(value.length() - 4);
    }

    private boolean isSecretKey(String key) {
        return "aliyun_sms_access_key_id".equals(key)
                || "aliyun_sms_access_key_secret".equals(key);
    }

    @GetMapping
    @RequirePermission(AdminPermission.CONFIG_VIEW)
    public Result<Map<String, String>> getAll() {
        Map<String, String> raw = platformConfigService.getSmsConfig();
        Map<String, String> masked = new HashMap<>();
        raw.forEach((k, v) -> {
            if (isSecretKey(k)) {
                masked.put(k, maskSecret(v));
            } else {
                masked.put(k, v);
            }
        });
        return Result.success(masked);
    }

    @GetMapping("/{key}")
    @RequirePermission(AdminPermission.CONFIG_VIEW)
    public Result<String> getOne(@PathVariable String key) {
        String value = platformConfigService.getValue(key);
        if (isSecretKey(key)) {
            value = maskSecret(value);
        }
        return Result.success(value);
    }

    @PutMapping
    @RequirePermission(AdminPermission.CONFIG_EDIT)
    public Result<Void> update(@RequestBody Map<String, String> config) {
        platformConfigService.saveSmsConfig(config);
        return Result.success(null);
    }
}