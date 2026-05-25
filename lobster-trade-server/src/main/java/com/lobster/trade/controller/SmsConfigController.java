package com.lobster.trade.controller;

import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.service.SmsConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员短信配置接口
 * 路径：/api/admin/sms-config
 */
@RestController
@RequestMapping("/api/admin/sms-config")
@RequiredArgsConstructor
public class SmsConfigController {

    private final SmsConfigService smsConfigService;

    /**
     * 获取短信配置（GET /api/admin/sms-config）
     */
    @GetMapping
    @RequirePermission(AdminPermission.CONFIG_VIEW)
    public Result<Map<String, String>> getSmsConfig() {
        return Result.success(smsConfigService.getSmsConfig());
    }

    /**
     * 保存短信配置（PUT /api/admin/sms-config）
     */
    @PutMapping
    @RequirePermission(AdminPermission.CONFIG_EDIT)
    public Result<Void> saveSmsConfig(@RequestBody Map<String, String> config) {
        smsConfigService.saveSmsConfig(config);
        return Result.success(null);
    }
}