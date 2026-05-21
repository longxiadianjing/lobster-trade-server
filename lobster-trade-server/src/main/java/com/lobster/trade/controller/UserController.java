package com.lobster.trade.controller;

import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.entity.UserLoginDevice;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.SecurityCenterService;
import com.lobster.trade.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityCenterService securityCenterService;

    /**
     * 获取用户登录日志
     * GET /api/user/login-logs
     */
    @GetMapping("/login-logs")
    public ApiResponse<List<UserLoginDevice>> getLoginLogs(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(securityCenterService.getLoginHistory(userId));
    }
    /**
     * 获取用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    public ApiResponse<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     * PUT /api/user/info
     */
    @PutMapping("/info")
    public ApiResponse<Void> updateUserInfo(HttpServletRequest request,
                                            @RequestBody User updateUser) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserInfo(userId, updateUser);
        return ApiResponse.success("更新成功");
    }

    /**
     * 设置支付密码
     * POST /api/user/pay-password/set
     */
    @PostMapping("/pay-password/set")
    public ApiResponse<Void> setPayPassword(HttpServletRequest request,
                                           @Valid @RequestBody SetPayPasswordRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        userService.setPayPassword(userId, req.getPayPassword());
        return ApiResponse.success("支付密码设置成功");
    }

    /**
     * 申请实名认证
     * POST /api/user/real-name/apply
     */
    @PostMapping("/real-name/apply")
    public ApiResponse<Void> applyRealName(HttpServletRequest request,
                                           @Valid @RequestBody ApplyRealNameRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        userService.applyRealName(userId, req.getRealName(), req.getIdCard());
        return ApiResponse.success("实名认证申请已提交");
    }

    /**
     * 查询实名状态
     * GET /api/user/real-name/status
     */
    @GetMapping("/real-name/status")
    public ApiResponse<RealNameStatusVO> getRealNameStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RealNameStatusVO status = userService.getRealNameStatus(userId);
        return ApiResponse.success(status);
    }

    // ===== 内部请求类 =====

    @lombok.Data
    public static class SetPayPasswordRequest {
        @NotBlank(message = "支付密码不能为空")
        private String payPassword;
    }

    @lombok.Data
    public static class ApplyRealNameRequest {
        @NotBlank(message = "真实姓名不能为空")
        private String realName;
        @NotBlank(message = "身份证号不能为空")
        private String idCard;
    }

    // ===== 实名状态VO =====
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RealNameStatusVO {
        private Integer status;       // 0-未实名，1-已实名，2-审核中，3-未通过
        private String realName;      // 真实姓名（脱敏）
        private String idCard;       // 身份证号（脱敏）
        private String auditRemark;  // 审核备注
    }
}
