package com.lobster.trade.service.impl;

import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.service.JwtAuthService;
import com.lobster.trade.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtAuthServiceImpl implements JwtAuthService {

    private final UserMapper userMapper;

    @Override
    public Long getUserIdFromToken(String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无效的认证凭证");
        }
        // 检查用户是否被封禁
        User user = userMapper.selectById(userId);
        if (user != null && user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被封禁，请联系客服");
        }
        return userId;
    }
}
