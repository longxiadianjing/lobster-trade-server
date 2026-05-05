package com.lobster.trade.controller;

import com.lobster.trade.common.Result;
import com.lobster.trade.service.RealNameVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/real-name")
@RequiredArgsConstructor
class RealNameApiController {

    private final RealNameVerifyService verifyService;

    @PostMapping("/init")
    public Result<Map<String, String>> initVerify(@RequestAttribute Long userId) {
        return Result.success(verifyService.initCertification(userId));
    }

    @PostMapping("/callback")
    public Result<Void> aliyunCallback(@RequestBody Map<String, Object> body) {
        verifyService.handleAliyunCallback(body);
        return Result.success(null);
    }

    @PostMapping("/apply")
    public Result<Void> applyRealName(@RequestAttribute Long userId, @RequestBody Map<String, String> body) {
        verifyService.applyRealName(userId, body.get("realName"), body.get("idCard"));
        return Result.success(null);
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus(@RequestAttribute Long userId) {
        return Result.success(verifyService.getCertificationStatus(userId));
    }

    @PostMapping("/verify-result")
    public Result<Map<String, Object>> getVerifyResult(@RequestAttribute Long userId) {
        return Result.success(verifyService.getVerifyResult(userId));
    }
}