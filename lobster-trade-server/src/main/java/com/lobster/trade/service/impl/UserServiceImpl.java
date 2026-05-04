package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.mapper.WalletMapper;
import com.lobster.trade.model.entity.User;
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
    private final WalletMapper walletMapper;

    @Override
    public User getCurrentUser(Long userId) {
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

        // TODO: 对接公安实名API进行真实认证
        // 目前模拟：直接审核通过
        user.setRealName(realName);
        user.setIdCard(idCard);
        user.setRealNameStatus(1); // 直接设为已实名（生产环境应设为2审核中）
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

        return vo;
    }
}
