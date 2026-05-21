package com.lobster.trade.controller;

import com.lobster.trade.model.entity.UserLoginDevice;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.SecurityCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityCenterController {

    private final SecurityCenterService securityService;

    @GetMapping("/score")
    public ApiResponse<Map<String, Object>> getSecurityScore(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int score = securityService.getSecurityScore(userId);
        List<String> events = securityService.getSecurityEvents(userId);
        return ApiResponse.success(Map.of(
            "score", score,
            "level", scoreLevel(score),
            "events", events
        ));
    }

    @GetMapping("/devices")
    public ApiResponse<List<UserLoginDevice>> getDevices(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(securityService.getMyDevices(userId));
    }

    @PostMapping("/devices/{deviceId}/toggle-trust")
    public ApiResponse<Void> toggleTrustDevice(
            @PathVariable Long deviceId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        securityService.toggleTrustDevice(userId, deviceId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/devices/{deviceId}")
    public ApiResponse<Void> removeDevice(
            @PathVariable Long deviceId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        securityService.removeDevice(userId, deviceId);
        return ApiResponse.success(null);
    }

    @GetMapping("/events")
    public ApiResponse<List<String>> getEvents(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(securityService.getSecurityEvents(userId));
    }

    @GetMapping("/login-history")
    public ApiResponse<List<UserLoginDevice>> getLoginHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(securityService.getLoginHistory(userId));
    }

    @PostMapping("/device/login")
    public ApiResponse<Void> recordDeviceLogin(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
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
}
