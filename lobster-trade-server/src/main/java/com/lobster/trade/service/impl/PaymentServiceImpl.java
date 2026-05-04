package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.PaymentTransactionMapper;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.mapper.WalletMapper;
import com.lobster.trade.model.entity.PaymentTransaction;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.entity.Wallet;
import com.lobster.trade.model.entity.WalletTransaction;
import com.lobster.trade.service.PaymentService;
import com.lobster.trade.service.WalletService;
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
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionMapper paymentMapper;
    private final TradeOrderMapper orderMapper;
    private final WalletService walletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentTransaction createRechargePayment(Long userId, BigDecimal amount, String channel) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "充值金额必须大于0");
        }

        PaymentTransaction payment = new PaymentTransaction();
        payment.setPaymentNo("PAY" + SnowflakeIdUtil.generateOrderNo());
        payment.setUserId(userId);
        payment.setOrderId(null);
        payment.setPaymentType(PaymentTransaction.TYPE_RECHARGE);
        payment.setAmount(amount);
        payment.setChannel(channel);
        payment.setStatus(PaymentTransaction.STATUS_PENDING);
        payment.setExpireTime(LocalDateTime.now().plusMinutes(10));
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        log.info("[PAYMENT] 创建充值支付单: {}, userId={}, amount={}, channel={}",
            payment.getPaymentNo(), userId, amount, channel);

        return payment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentTransaction createOrderPayment(Long userId, Long orderId, String channel) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权支付此订单");
        }
        if (!"pending_pay".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "当前状态不支持支付");
        }

        PaymentTransaction payment = new PaymentTransaction();
        payment.setPaymentNo("PAY" + SnowflakeIdUtil.generateOrderNo());
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        payment.setPaymentType(PaymentTransaction.TYPE_ORDER);
        payment.setAmount(order.getEscrowAmount());
        payment.setChannel(channel);
        payment.setStatus(PaymentTransaction.STATUS_PENDING);
        payment.setExpireTime(LocalDateTime.now().plusMinutes(10));
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        log.info("[PAYMENT] 创建订单支付单: {}, orderId={}, amount={}, channel={}",
            payment.getPaymentNo(), orderId, order.getEscrowAmount(), channel);

        return payment;
    }

    @Override
    public PaymentTransaction getPaymentStatus(Long userId, String paymentNo) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getPaymentNo, paymentNo)
               .eq(PaymentTransaction::getUserId, userId)
               .eq(PaymentTransaction::getIsDeleted, 0);
        PaymentTransaction payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "支付单不存在");
        }
        // 检查过期
        if (payment.getStatus() == PaymentTransaction.STATUS_PENDING
            && payment.getExpireTime() != null
            && payment.getExpireTime().isBefore(LocalDateTime.now())) {
            payment.setStatus(PaymentTransaction.STATUS_EXPIRED);
            payment.setUpdateTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
        }
        return payment;
    }

    @Override
    @Transactional
    public void processMockCallback(String paymentNo) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getPaymentNo, paymentNo);
        PaymentTransaction payment = paymentMapper.selectOne(wrapper);

        if (payment == null) {
            log.warn("[PAYMENT] Mock回调找不到支付单: {}", paymentNo);
            return;
        }
        if (payment.getStatus() != PaymentTransaction.STATUS_PENDING) {
            log.warn("[PAYMENT] 支付单状态不是待支付，跳过: {}, status={}", paymentNo, payment.getStatus());
            return;
        }

        // 标记为成功
        payment.setStatus(PaymentTransaction.STATUS_SUCCESS);
        payment.setTransactionId("MOCK_" + SnowflakeIdUtil.generateOrderNo());
        payment.setPaidTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        log.info("[PAYMENT] Mock支付成功: {}, amount={}", paymentNo, payment.getAmount());

        // 根据类型处理业务
        if (PaymentTransaction.TYPE_RECHARGE.equals(payment.getPaymentType())) {
            handleRechargeSuccess(payment);
        } else if (PaymentTransaction.TYPE_ORDER.equals(payment.getPaymentType())) {
            handleOrderPaySuccess(payment);
        }
    }

    @Override
    public PaymentTransaction getByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<PaymentTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransaction::getPaymentNo, paymentNo);
        return paymentMapper.selectOne(wrapper);
    }

    private void handleRechargeSuccess(PaymentTransaction payment) {
        // 调用钱包充值
        walletService.rechargeMock(payment.getUserId(), payment.getAmount(), payment.getPaymentNo());
        log.info("[PAYMENT] 充值到账: userId={}, amount={}, paymentNo={}",
            payment.getUserId(), payment.getAmount(), payment.getPaymentNo());
    }

    private void handleOrderPaySuccess(PaymentTransaction payment) {
        // 调用订单支付成功处理
        walletService.freezeEscrowForOrder(payment.getOrderId());
        log.info("[PAYMENT] 订单支付成功: orderId={}, paymentNo={}", payment.getOrderId(), payment.getPaymentNo());
    }
}