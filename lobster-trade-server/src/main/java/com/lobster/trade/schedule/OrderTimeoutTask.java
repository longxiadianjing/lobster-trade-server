package com.lobster.trade.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.service.EscrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时取消定时任务
 * - 待付款订单超过30分钟自动取消
 * - 卖家发货后买家48小时未确认，自动标记超时
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final TradeOrderMapper tradeOrderMapper;
    private final EscrowService escrowService;

    /** 每5分钟执行一次 */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void cancelPendingOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getStatus, "pending_pay")
               .eq(TradeOrder::getIsDeleted, 0)
               .le(TradeOrder::getCreateTime, deadline);
        List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
        if (orders.isEmpty()) return;
        log.info("[ORDER_TIMEOUT] 扫描到 {} 个待付款超时订单", orders.size());
        for (TradeOrder order : orders) {
            try {
                order.setStatus("cancelled");
                order.setRefundRequest(1);
                order.setRefundReason("超时自动取消");
                tradeOrderMapper.updateById(order);
                // 退款（如果有冻结资金）
                escrowService.refundEscrow(order);
                // 库存还原
                log.info("[ORDER_TIMEOUT] 订单 {} 已超时取消", order.getOrderNo());
            } catch (Exception e) {
                log.error("[ORDER_TIMEOUT] 取消订单 {} 失败: {}", order.getOrderNo(), e.getMessage());
            }
        }
    }

    /** 每10分钟检查卖家发货超时（已支付但超过48小时未发货） */
    @Scheduled(fixedRate = 600_000)
    public void checkSellerDeliveryTimeout() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(48);
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getStatus, "paid")
               .eq(TradeOrder::getIsDeleted, 0)
               .le(TradeOrder::getPaymentTime, deadline);
        List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
        if (orders.isEmpty()) return;
        log.warn("[SELLER_TIMEOUT] 发现 {} 个卖家发货超时订单，请客服关注", orders.size());
        for (TradeOrder order : orders) {
            log.warn("[SELLER_TIMEOUT] 订单 {} 超时未发货，卖家={}, 支付时间={}",
                order.getOrderNo(), order.getSellerId(), order.getPaymentTime());
            // 不自动取消，留给客服处理或买家发起纠纷
        }
    }
}