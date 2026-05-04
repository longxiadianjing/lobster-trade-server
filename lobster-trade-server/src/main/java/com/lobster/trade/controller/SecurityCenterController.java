package com.lobster.trade.controller;

import com.lobster.trade.model.entity.UserLoginDevice;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.SecurityCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityCenterController {

    private final SecurityCenterService securityService;

    /**
     * 获取账户安全评分
     * GET /api/security/score
     */
    @GetMapping("/score")
    public ApiResponse<Map<String, Object>> getSecurityScore(
            @RequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);
        int score = securityService.getSecurityScore(userId);
        List<String> events = securityService.getSecurityEvents(userId);
        return ApiResponse.success(Map.of(
            "score", score,
            "level", scoreLevel(score),
            "events", events
        ));
    }

    /**
     * 获取登录设备列表
     * GET /api/security/devices
     */
    @GetMapping("/devices")
    public ApiResponse<List<UserLoginDevice>> getDevices(
            @RequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);
        return ApiResponse.success(securityService.getMyDevices(userId));
    }

    /**
     * 切换设备可信状态
     * PUT /api/security/devices/{deviceId}/trust
     */
    @PutMapping("/devices/{deviceId}/trust")
    public ApiResponse<Void> toggleTrustDevice(
            @PathVariable Long deviceId,
            @RequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);
        securityService.toggleTrustDevice(userId, deviceId);
        return ApiResponse.success(null);
    }

    /**
     * 移除登录设备
     * DELETE /api/security/devices/{deviceId}
     */
    @DeleteMapping("/devices/{deviceId}")
    public ApiResponse<Void> removeDevice(
            @PathVariable Long deviceId,
            @RequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);
        securityService.removeDevice(userId, deviceId);
        return ApiResponse.success(null);
    }

    /**
     * 记录当前设备登录
     * POST /api/security/device/login
     * Body: { deviceFingerprint, deviceName, ipAddress }
     */
    @PostMapping("/device/login")
    public ApiResponse<Void> recordDeviceLogin(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);
        String fp = body.get("deviceFingerprint");
        String name = body.get("deviceName");
        String ip = body.get("ipAddress");
        if (fp != null && name != null && ip != null) {
            securityService.recordDeviceLogin(userId, fp, name, ip);
        }
        return ApiResponse.success(null);
    }

    private String scoreLevel(int score) {
        if (score >= 90) return "高";
        if (score >= 60) return "中";
        return "低";
    }

    private Long extractUserId(String auth) {
        try {
            String token = auth.substring(7);
            java.util.Map<String, Object> payload = com.lobster.trade.util.JwtUtil.verifyToken(token);
            Object uid = payload.get("userId");
            if (uid instanceof Integer) return ((Integer) uid).longValue();
            if (uid instanceof Long) return (Long) uid;
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
