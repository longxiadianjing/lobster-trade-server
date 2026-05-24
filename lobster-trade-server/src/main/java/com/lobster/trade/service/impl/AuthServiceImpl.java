package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.mapper.SmsCodeMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.mapper.WalletMapper;
import com.lobster.trade.model.entity.SmsCode;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.entity.Wallet;
import com.lobster.trade.model.request.*;
import com.lobster.trade.model.response.LoginResponse;
import com.lobster.trade.service.AuthService;
import com.lobster.trade.service.SecurityCenterService;
import com.lobster.trade.util.AliyunSmsUtil;
import com.lobster.trade.util.JwtUtil;
import com.lobster.trade.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final SecurityCenterService securityCenterService;
    private final SmsCodeMapper smsCodeMapper;
    private final JwtUtil jwtUtil;
    private final AliyunSmsUtil aliyunSmsUtil;
    private final RedisTemplate<String, String> redisTemplate;

    /** 短信验证码有效期（分钟） */
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;

    /** 短信验证码长度 */
    private static final int SMS_CODE_LENGTH = 6;

    @Value("${jwt.expire:86400000}")
    private Long tokenExpire;

    @Override
    public void sendSmsCode(SmsSendRequest request) {
        String phone = request.getPhone();
        String type = request.getType();

        // 校验手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }

        // 同一类型验证码发送间隔校验（60秒）
        String intervalKey = "sms:interval:" + phone + ":" + type;
        Boolean intervalSet = redisTemplate.opsForValue().setIfAbsent(intervalKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(intervalSet)) {
            throw new BusinessException("发送过于频繁，请稍后再试");
        }

        // 生成验证码
        String code = generateSmsCode();

        // 存储到Redis
        String cacheKey = "sms:code:" + phone + ":" + type;
        redisTemplate.opsForValue().set(cacheKey, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 通过阿里云短信发送真实验证码
        boolean smsSent = aliyunSmsUtil.sendVerifyCode(phone, code);
        if (!smsSent) {
            log.warn("[短信] 阿里云发送失败，手机号={}，验证码={}（已存入Redis，仍可用于测试）", phone, code);
        }

        // 也存入数据库便于校验
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setType(type);
        smsCode.setExpireTime(LocalDateTime.now().plusMinutes(SMS_CODE_EXPIRE_MINUTES));
        smsCode.setUsed(0);
        smsCode.setCreateTime(LocalDateTime.now());
        smsCodeMapper.insert(smsCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        String password = request.getPassword();

        // 校验验证码
        validateSmsCode(phone, code, "register");

        // 校验手机号是否已注册
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, phone);
        if (userMapper.selectCount(phoneWrapper) > 0) {
            throw new BusinessException("该手机号已注册");
        }

        // 生成用户名（手机号脱敏）
        String username = "user_" + phone.substring(0, 3) + "****" + phone.substring(7);

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setNickname(username);
        user.setPhone(phone);
        user.setPassword(PasswordEncoder.encode(password));
        user.setRealNameStatus(0);
        user.setUserLevel(1);
        user.setReputationScore(new java.math.BigDecimal("5.00"));
        user.setTotalTradeCount(0);
        user.setTotalTradeAmount(new java.math.BigDecimal("0.00"));
        user.setBalance(new java.math.BigDecimal("0.00"));
        user.setFrozenBalance(new java.math.BigDecimal("0.00"));
        user.setStatus(1);
        user.setRegisterSource("H5");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 创建钱包
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(new java.math.BigDecimal("0.00"));
        wallet.setFrozenBalance(new java.math.BigDecimal("0.00"));
        wallet.setTotalIncome(new java.math.BigDecimal("0.00"));
        wallet.setTotalExpense(new java.math.BigDecimal("0.00"));
        wallet.setPasswordSet(0);
        wallet.setCreateTime(LocalDateTime.now());
        wallet.setUpdateTime(LocalDateTime.now());
        walletMapper.insert(wallet);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId());

        return new LoginResponse(token, user.getId(), user.getNickname(), user.getAvatar());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("手机号或密码错误");
        }

        // 校验密码
        if (!PasswordEncoder.matches(user.getPassword(), password)) {
            throw new BusinessException("手机号或密码错误");
        }

        // 校验账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系客服");
        }

        // 更新最后登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId());

        return new LoginResponse(token, user.getId(), user.getNickname(), user.getAvatar());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        String newPassword = request.getPassword();

        // 校验验证码
        validateSmsCode(phone, code, "reset_password");

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }

        // 更新密码
        user.setPassword(PasswordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户 {} 重置密码成功", phone);
    }

    /**
     * 校验短信验证码
     */
    private void validateSmsCode(String phone, String code, String type) {
        // 先从Redis校验
        String cacheKey = "sms:code:" + phone + ":" + type;
        String cachedCode = redisTemplate.opsForValue().get(cacheKey);

        if (cachedCode != null && cachedCode.equals(code)) {
            // 验证通过，删除Redis中的验证码
            redisTemplate.delete(cacheKey);
            return;
        }

        // Redis没有再查数据库（兼容未存入Redis的情况）
        LambdaQueryWrapper<SmsCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsCode::getPhone, phone)
                .eq(SmsCode::getCode, code)
                .eq(SmsCode::getType, type)
                .eq(SmsCode::getUsed, 0)
                .gt(SmsCode::getExpireTime, LocalDateTime.now())
                .orderByDesc(SmsCode::getCreateTime)
                .last("LIMIT 1");

        SmsCode smsCode = smsCodeMapper.selectOne(wrapper);

        if (smsCode == null) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 标记验证码已使用
        smsCode.setUsed(1);
        smsCode.setUsedTime(LocalDateTime.now());
        smsCodeMapper.updateById(smsCode);
    }

    /**
     * 生成短信验证码
     */
    private String generateSmsCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < SMS_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    @Override
    public boolean isTokenExpired(String token) {
        if (token == null || token.isEmpty()) return true;
        try {
            return jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public LoginResponse refreshToken(String oldToken) {
        // 解析旧Token获取userId
        Long userId = JwtUtil.getUserIdFromToken(oldToken);
        if (userId == null) {
            throw new BusinessException("无效的Token");
        }
        // 生成新Token
        String newToken = jwtUtil.generateToken(userId);
        // 查用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        LoginResponse response = new LoginResponse();
        response.setToken(newToken);
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        return response;
    }
}
