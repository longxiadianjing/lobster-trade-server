package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.mapper.WalletMapper;
import com.lobster.trade.mapper.WalletTransactionMapper;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.entity.Wallet;
import com.lobster.trade.model.entity.WalletTransaction;
import com.lobster.trade.service.EscrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lobster.trade.util.SnowflakeIdUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscrowServiceImpl implements EscrowService {

    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeEscrow(TradeOrder order) {
        Wallet buyerWallet = getWallet(order.getBuyerId());
        BigDecimal amount = order.getEscrowAmount();

        BigDecimal balanceBefore = buyerWallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        BigDecimal frozenBefore = buyerWallet.getFrozenBalance();
        BigDecimal frozenAfter = frozenBefore.add(amount);

        buyerWallet.setBalance(balanceAfter);
        buyerWallet.setFrozenBalance(frozenAfter);
        walletMapper.updateById(buyerWallet);

        // 记录冻结：余额减少
        saveTransaction(order.getBuyerId(), 3, amount.negate(),
            "资金冻结 #" + order.getOrderNo(), "冻结", order.getId(),
            balanceBefore, balanceAfter, frozenBefore, frozenAfter);

        log.info("[ESCROW] 资金已冻结: orderNo={}, buyerId={}, amount={}, balance {} -> {}, frozen {} -> {}",
            order.getOrderNo(), order.getBuyerId(), amount, balanceBefore, balanceAfter, frozenBefore, frozenAfter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseEscrow(TradeOrder order) {
        BigDecimal amount = order.getEscrowAmount();
        BigDecimal platformFee = order.getPlatformFee();
        BigDecimal sellerReceived = order.getSellerReceived();

        // 解冻买家资金
        Wallet buyerWallet = getWallet(order.getBuyerId());
        BigDecimal bBalBefore = buyerWallet.getBalance();
        BigDecimal bFrozenBefore = buyerWallet.getFrozenBalance();
        buyerWallet.setFrozenBalance(bFrozenBefore.subtract(amount));
        walletMapper.updateById(buyerWallet);

        saveTransaction(order.getBuyerId(), 4, amount,
            "资金解冻 #" + order.getOrderNo(), "解冻", order.getId(),
            bBalBefore, bBalBefore, bFrozenBefore, bFrozenBefore.subtract(amount));

        // 增加卖家余额（扣除手续费）
        Wallet sellerWallet = getWallet(order.getSellerId());
        BigDecimal sBalBefore = sellerWallet.getBalance();
        BigDecimal sBalAfter = sBalBefore.add(sellerReceived);
        sellerWallet.setBalance(sBalAfter);
        sellerWallet.setTotalIncome(sellerWallet.getTotalIncome().add(sellerReceived));
        walletMapper.updateById(sellerWallet);

        // 平台手续费记录
        Wallet platformWallet = getPlatformWallet();
        BigDecimal pBalBefore = platformWallet.getBalance();
        platformWallet.setBalance(pBalBefore.add(platformFee));
        platformWallet.setTotalIncome(platformWallet.getTotalIncome().add(platformFee));
        walletMapper.updateById(platformWallet);

        saveTransaction(order.getSellerId(), 1, sellerReceived,
            "订单完成 #" + order.getOrderNo(), "收入", order.getId(),
            sBalBefore, sBalAfter, BigDecimal.ZERO, BigDecimal.ZERO);

        saveTransaction(1L, 1, platformFee,
            "手续费收入 #" + order.getOrderNo(), "佣金", order.getId(),
            pBalBefore, pBalBefore.add(platformFee), BigDecimal.ZERO, BigDecimal.ZERO);

        log.info("[ESCROW] 资金已释放: orderNo={}, amount={}, sellerId={} 收到={}, platformFee={}",
            order.getOrderNo(), amount, order.getSellerId(), sellerReceived, platformFee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundEscrow(TradeOrder order) {
        Wallet buyerWallet = getWallet(order.getBuyerId());
        BigDecimal amount = order.getEscrowAmount();

        BigDecimal bBalBefore = buyerWallet.getBalance();
        BigDecimal bFrozenBefore = buyerWallet.getFrozenBalance();

        buyerWallet.setFrozenBalance(bFrozenBefore.subtract(amount));
        buyerWallet.setBalance(bBalBefore.add(amount));
        walletMapper.updateById(buyerWallet);

        saveTransaction(order.getBuyerId(), 5, amount,
            "订单取消退款 #" + order.getOrderNo(), "退款", order.getId(),
            bBalBefore, bBalBefore.add(amount), bFrozenBefore, bFrozenBefore.subtract(amount));

        order.setEscrowStatus(4);
        log.info("[ESCROW] 资金已退款: orderNo={}, buyerId={}, amount={}",
            order.getOrderNo(), order.getBuyerId(), amount);
    }

    private Wallet getWallet(Long userId) {
        LambdaQueryWrapper<Wallet> w = new LambdaQueryWrapper<>();
        w.eq(Wallet::getUserId, userId);
        Wallet wallet = walletMapper.selectOne(w);
        if (wallet == null) {
            throw new RuntimeException("钱包不存在: userId=" + userId);
        }
        return wallet;
    }

    private Wallet getPlatformWallet() {
        return getWallet(1L);
    }

    private void saveTransaction(Long userId, int type, BigDecimal amount,
            String remark, String source, Long orderId,
            BigDecimal balanceBefore, BigDecimal balanceAfter,
            BigDecimal frozenBefore, BigDecimal frozenAfter) {
        WalletTransaction txn = new WalletTransaction();
        txn.setTransNo(SnowflakeIdUtil.generateTransNo());
        txn.setUserId(userId);
        txn.setType(type);
        txn.setAmount(amount);
        txn.setBalanceBefore(balanceBefore);
        txn.setBalanceAfter(balanceAfter);
        txn.setFrozenBefore(frozenBefore);
        txn.setFrozenAfter(frozenAfter);
        txn.setRemark(remark);
        txn.setSource(source);
        txn.setSourceId(orderId);
        txn.setStatus(1);
        txn.setCreateTime(LocalDateTime.now());
        walletTransactionMapper.insert(txn);
    }
}