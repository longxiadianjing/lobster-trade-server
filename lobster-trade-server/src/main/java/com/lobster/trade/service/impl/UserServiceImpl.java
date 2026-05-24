package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.mapper.UserRealNameMapper;
import com.lobster.trade.mapper.WalletMapper;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.entity.UserRealName;
import com.lobster.trade.model.entity.Wallet;
import com.lobster.trade.service.UserService;
import com.lobster.trade.util.PasswordEncoder;
import com.lobster.trade.controller.UserController.RealNameStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRealNameMapper userRealNameMapper;
    private final WalletMapper walletMapper;

    @Override
    public User getCurrentUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户未登录或登录已失效");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        user.setPayPassword(null);
        return user;
    }

    @Override
    public void updateUserInfo(Long userId, User updateUser) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (updateUser.getNickname() != null) {
            user.setNickname(updateUser.getNickname());
        }
        if (updateUser.getAvatar() != null) {
            user.setAvatar(updateUser.getAvatar());
        }
        if (updateUser.getEmail() != null) {
            user.setEmail(updateUser.getEmail());
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPayPassword(Long userId, String payPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 设置用户的支付密码
        user.setPayPassword(PasswordEncoder.encode(payPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 同时更新钱包表的支付密码标记
        LambdaQueryWrapper<Wallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wallet::getUserId, userId);
        Wallet wallet = walletMapper.selectOne(wrapper);
        if (wallet != null) {
            wallet.setPasswordSet(1);
            wallet.setPassword(PasswordEncoder.encode(payPassword));
            wallet.setUpdateTime(LocalDateTime.now());
            walletMapper.updateById(wallet);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRealName(Long userId, String realName, String idCard) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getRealNameStatus() != null && user.getRealNameStatus() == 1) {
            throw new BusinessException("已实名认证，无需重复申请");
        }

        // 写入 user_real_name 表（状态=0 审核中）
        UserRealName record = new UserRealName();
        record.setUserId(userId);
        record.setRealName(realName);
        record.setIdCard(idCard);
        record.setStatus(0);
        userRealNameMapper.insert(record);

        // 用户表设为审核中
        user.setRealNameStatus(2);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public RealNameStatusVO getRealNameStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        RealNameStatusVO vo = new RealNameStatusVO();
        vo.setStatus(user.getRealNameStatus() != null ? user.getRealNameStatus() : 0);

        if (user.getRealName() != null && user.getRealNameStatus() == 1) {
            // 脱敏真实姓名
            String name = user.getRealName();
            vo.setRealName(name.charAt(0) + "**");
        } else {
            vo.setRealName(null);
        }

        if (user.getIdCard() != null && user.getRealNameStatus() == 1) {
            // 脱敏身份证号
            String idCard = user.getIdCard();
            vo.setIdCard(idCard.substring(0, 6) + "********" + idCard.substring(14));
        } else {
            vo.setIdCard(null);
        }

        // 从 user_real_name 表取最新审核备注
        if (user.getRealNameStatus() == 2 || user.getRealNameStatus() == 3) {
            LambdaQueryWrapper<UserRealName> q = new LambdaQueryWrapper<>();
            q.eq(UserRealName::getUserId, userId)
             .orderByDesc(UserRealName::getCreateTime)
             .last("LIMIT 1");
            UserRealName latest = userRealNameMapper.selectOne(q);
            if (latest != null) {
                vo.setAuditRemark(latest.getRejectReason());
            }
        }

        return vo;
    }
}
