package com.lobster.trade.service.impl;

import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.service.JwtAuthService;
import com.lobster.trade.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtAuthServiceImpl implements JwtAuthService {

    @Override
    public Long getUserIdFromToken(String token) {
        try {
            return JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无效的认证凭证");
        }
    }
}
