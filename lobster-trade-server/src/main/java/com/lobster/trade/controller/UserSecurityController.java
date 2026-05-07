package com.lobster.trade.controller;

import com.lobster.trade.model.entity.UserLoginDevice;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.SecurityCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户账户安全 API（供前端 /security/center 页面使用）
 * 挂载路径：/api/security/*
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class UserSecurityController {

    private final SecurityCenterService securityCenterService;

    /**
     * 获取可信设备列表
     * GET /api/security/devices
     */
    @GetMapping("/devices")
    public ApiResponse<List<UserLoginDevice>> getDevices(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(securityCenterService.getMyDevices(userId));
    }

    /**
     * 标记/取消可信设备
     * POST /api/security/devices/{deviceId}/toggle-trust
     */
    @PostMapping("/devices/{deviceId}/toggle-trust")
    public ApiResponse<Void> toggleTrust(
            @PathVariable Long deviceId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        securityCenterService.toggleTrustDevice(userId, deviceId);
        return ApiResponse.success("操作成功");
    }

    /**
     * 移除设备
     * DELETE /api/security/devices/{deviceId}
     */
    @DeleteMapping("/devices/{deviceId}")
    public ApiResponse<Void> removeDevice(
            @PathVariable Long deviceId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        securityCenterService.removeDevice(userId, deviceId);
        return ApiResponse.success("设备已移除");
    }

    /**
     * 获取安全评分及详细信息
     * GET /api/security/score
     */
    @GetMapping("/score")
    public ApiResponse<Map<String, Object>> getScore(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int score = securityCenterService.getSecurityScore(userId);
        List<String> events = securityCenterService.getSecurityEvents(userId);
        return ApiResponse.success(Map.of(
            "score", score,
            "level", scoreLevel(score),
            "events", events
        ));
    }

    /**
     * 获取安全动态
     * GET /api/security/events
     */
    @GetMapping("/events")
    public ApiResponse<List<String>> getEvents(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(securityCenterService.getSecurityEvents(userId));
    }

    /**
     * 获取登录历史
     * GET /api/security/login-history
     */
    @GetMapping("/login-history")
    public ApiResponse<List<UserLoginDevice>> getLoginHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(securityCenterService.getLoginHistory(userId));
    }

    private String scoreLevel(int score) {
        if (score >= 90) return "高";
        if (score >= 60) return "中";
        return "低";
    }
}