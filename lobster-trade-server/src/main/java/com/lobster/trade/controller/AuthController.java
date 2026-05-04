package com.lobster.trade.controller;

import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.request.*;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.model.response.LoginResponse;
import com.lobster.trade.service.AuthService;
import com.lobster.trade.util.PasswordEncoder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    /**
     * 发送短信验证码
     * POST /api/auth/sms/send
     */
    @PostMapping("/sms/send")
    public ApiResponse<Void> sendSmsCode(@Validated @RequestBody SmsSendRequest request) {
        authService.sendSmsCode(request);
        return ApiResponse.success("验证码已发送");
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Validated @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ApiResponse.success("注册成功", response);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 调试接口：测试密码验证
     */
    @GetMapping("/debug/password")
    public ApiResponse<String> debugPassword() {
        String hash = "$2a$10$xEgxxYU3cnXwl2yBwxG6ueINegsOz3HYzJjAdjwXAfF0UJpFRxGt.";
        boolean ok = PasswordEncoder.matches(hash, "Test123456");
        return ApiResponse.success("matches=" + ok);
    }

    /**
     * 调试接口：查询用户
     */
    @GetMapping("/debug/user")
    public ApiResponse<String> debugUser() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, "13812340001");
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return ApiResponse.success("user=null");
        }
        boolean ok = PasswordEncoder.matches(user.getPassword(), "Test123456");
        return ApiResponse.success("user.id=" + user.getId() + ", matches=" + ok);
    }

    /**
     * 调试接口：直接测试登录逻辑
     */
    @PostMapping("/debug/login")
    public ApiResponse<String> debugLogin(@RequestBody LoginRequest request) {
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, request.getPhone());
            User user = userMapper.selectOne(wrapper);
            if (user == null) {
                return ApiResponse.fail(400, "用户不存在");
            }
            boolean pwdOk = PasswordEncoder.matches(user.getPassword(), request.getPassword());
            return ApiResponse.success("user.id=" + user.getId() + ", pwdOk=" + pwdOk + ", status=" + user.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.fail(500, e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * 重置密码
     * POST /api/auth/password/reset
     */
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("密码重置成功");
    }

    /**
     * 刷新 Token（无感续期）
     * 条件：原Token未过期，且距离过期不足7天
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@RequestBody(required = false) Map<String, String> body) {
        String token = null;
        if (body != null && body.containsKey("token")) {
            token = body.get("token");
        }
        if (token == null) {
            return ApiResponse.fail(400, "token不能为空");
        }
        // 验证原Token未过期
        if (authService.isTokenExpired(token)) {
            return ApiResponse.fail(401, "token已过期，请重新登录");
        }
        // 刷新Token
        LoginResponse newToken = authService.refreshToken(token);
        return ApiResponse.success("刷新成功", newToken);
    }
}
