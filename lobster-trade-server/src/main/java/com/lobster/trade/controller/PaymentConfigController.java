package com.lobster.trade.controller;

import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.service.PaymentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员支付配置接口
 * 路径：/api/admin/payment-config
 */
@RestController
@RequestMapping("/api/admin/payment-config")
@RequiredArgsConstructor
public class PaymentConfigController {

    private final PaymentConfigService paymentConfigService;

    /**
     * 获取支付配置（GET /api/admin/payment-config）
     */
    @GetMapping
    @RequirePermission(AdminPermission.CONFIG_VIEW)
    public Result<Map<String, String>> getPaymentConfig() {
        return Result.success(paymentConfigService.getPaymentConfig());
    }

    /**
     * 保存支付配置（PUT /api/admin/payment-config）
     * 保存后自动刷新 AlipayConfig / WeChatConfig 等 Bean
     */
    @PutMapping
    @RequirePermission(AdminPermission.CONFIG_EDIT)
    public Result<Void> savePaymentConfig(@RequestBody Map<String, String> config) {
        paymentConfigService.savePaymentConfig(config);
        return Result.success(null);
    }

    /**
     * 手动刷新支付配置（POST /api/admin/payment-config/refresh）
     * 强制重新从数据库加载并刷新 Bean
     */
    @PostMapping("/refresh")
    @RequirePermission(AdminPermission.CONFIG_EDIT)
    public Result<Void> refreshPaymentConfig() {
        paymentConfigService.refreshPaymentConfigs();
        return Result.success(null);
    }
}