package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.mapper.WalletMapper;
import com.lobster.trade.mapper.WalletTransactionMapper;
import com.lobster.trade.model.entity.Wallet;
import com.lobster.trade.model.entity.WalletTransaction;
import com.lobster.trade.model.request.RechargeRequest;
import com.lobster.trade.model.request.WithdrawRequest;
import com.lobster.trade.service.WalletService;
import com.lobster.trade.util.PasswordEncoder;
import com.lobster.trade.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    public Wallet getWalletInfo(Long userId) {
        LambdaQueryWrapper<Wallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wallet::getUserId, userId);
        Wallet wallet = walletMapper.selectOne(wrapper);

        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }

        wallet.setPassword(null);
        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, RechargeRequest request) {
        BigDecimal amount = request.getAmount();
        String channel = request.getChannel();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }

        Wallet wallet = getWalletByUserId(userId);
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        wallet.setBalance(balanceAfter);
        wallet.setTotalIncome(wallet.getTotalIncome().add(amount));
        wallet.setUpdateTime(LocalDateTime.now());
        walletMapper.updateById(wallet);

        WalletTransaction trans = new WalletTransaction();
        trans.setTransNo(SnowflakeIdUtil.generateTransNo());
        trans.setUserId(userId);
        trans.setType(1);
        trans.setAmount(amount);
        trans.setBalanceBefore(balanceBefore);
        trans.setBalanceAfter(balanceAfter);
        trans.setFrozenBefore(BigDecimal.ZERO);
        trans.setFrozenAfter(BigDecimal.ZERO);
        trans.setSource("recharge");
        trans.setSourceNo(request.getChannel());
        trans.setStatus(1);
        trans.setRemark("钱包充值");
        trans.setCreateTime(LocalDateTime.now());
        walletTransactionMapper.insert(trans);

        log.info("用户 {} 充值成功，金额：{}，渠道：{}", userId, amount, channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long userId, WithdrawRequest request) {
        BigDecimal amount = request.getAmount();
        String channel = request.getChannel();
        String payPassword = request.getPayPassword();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("提现金额必须大于0");
        }
        if (amount.compareTo(new BigDecimal("10")) < 0) {
            throw new BusinessException("单笔提现最低10元");
        }
        if (amount.compareTo(new BigDecimal("50000")) > 0) {
            throw new BusinessException("单笔提现最高50000元");
        }

        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getPasswordSet() == 0 || !PasswordEncoder.matches(wallet.getPassword(), payPassword)) {
            throw new BusinessException("支付密码错误");
        }

        BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getFrozenBalance());
        if (amount.compareTo(availableBalance) > 0) {
            throw new BusinessException("可用余额不足");
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        wallet.setBalance(balanceAfter);
        wallet.setUpdateTime(LocalDateTime.now());
        walletMapper.updateById(wallet);

        WalletTransaction trans = new WalletTransaction();
        trans.setTransNo(SnowflakeIdUtil.generateTransNo());
        trans.setUserId(userId);
        trans.setType(2);
        trans.setAmount(amount);
        trans.setBalanceBefore(balanceBefore);
        trans.setBalanceAfter(balanceAfter);
        trans.setFrozenBefore(BigDecimal.ZERO);
        trans.setFrozenAfter(BigDecimal.ZERO);
        trans.setSource("withdraw");
        trans.setSourceNo(request.getChannel());
        trans.setStatus(3);
        trans.setRemark("提现申请");
        trans.setCreateTime(LocalDateTime.now());
        walletTransactionMapper.insert(trans);

        log.info("用户 {} 提交提现申请，金额：{}，渠道：{}", userId, amount, channel);
    }

    @Override
    public Page<WalletTransaction> getTransactions(Long userId, Integer page, Integer pageSize) {
        Page<WalletTransaction> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletTransaction::getUserId, userId)
                .orderByDesc(WalletTransaction::getCreateTime);
        return walletTransactionMapper.selectPage(pageParam, wrapper);
    }

    private Wallet getWalletByUserId(Long userId) {
        Wallet wallet = walletMapper.selectWalletWithPassword(userId);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }
        return wallet;
    }
}