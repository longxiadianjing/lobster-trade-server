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
@RequestMapping("/api/admin/payment-config")
@RequiredArgsConstructor
public class AdminPaymentConfigController {

    private final PlatformConfigService platformConfigService;

    /** 脱敏私钥：只显示 *** + 末尾4位 */
    private String maskSecret(String value) {
        if (value == null || value.isEmpty()) return null;
        if (value.length() <= 4) return "***";
        return "***" + value.substring(value.length() - 4);
    }

    private boolean isSecretKey(String key) {
        return "alipay_private_key".equals(key)
                || "alipay_public_key".equals(key)
                || "wechat_api_v3_key".equals(key)
                || "wechat_api_v2_key".equals(key)
                || "wechat_cert_content".equals(key);
    }

    @GetMapping
    @RequirePermission(AdminPermission.CONFIG_VIEW)
    public Result<Map<String, String>> getAll() {
        Map<String, String> raw = platformConfigService.getPaymentConfig();
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
        platformConfigService.savePaymentConfig(config);
        return Result.success(null);
    }
}