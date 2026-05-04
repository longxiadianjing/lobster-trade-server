package com.lobster.trade.service;

import com.lobster.trade.model.entity.Wallet;
import com.lobster.trade.model.entity.WalletTransaction;
import com.lobster.trade.model.request.RechargeRequest;
import com.lobster.trade.model.request.WithdrawRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface WalletService {

    /**
     * 获取钱包信息
     */
    Wallet getWalletInfo(Long userId);

    /**
     * 钱包充值（模拟）
     */
    void recharge(Long userId, RechargeRequest request);

    /**
     * 提现申请
     */
    void withdraw(Long userId, WithdrawRequest request);

    /**
     * 获取钱包流水
     */
    Page<WalletTransaction> getTransactions(Long userId, Integer page, Integer pageSize);
}
