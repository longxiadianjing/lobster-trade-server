package com.lobster.trade.service;

import com.lobster.trade.model.request.*;
import com.lobster.trade.model.response.LoginResponse;

public interface AuthService {

    /**
     * 发送短信验证码
     */
    void sendSmsCode(SmsSendRequest request);

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 重置密码
     */
    void resetPassword(ResetPasswordRequest request);
}
