package com.lobster.trade.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.model.entity.TradeOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单定时任务
 * 处理订单超时自动取消等逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    /** 订单超时取消时间（小时） */
    private static final int ORDER_TIMEOUT_HOURS = 24;

    private final TradeOrderMapper orderMapper;

    // 每 5 分钟扫描一次超时未付款的订单并自动取消
    // 执行周期: cron = "0 0/5 * * * ?" (每5分钟)
    @Scheduled(cron = "0 0/5 * * * ?")
    public void cancelExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(ORDER_TIMEOUT_HOURS);

        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getStatus, "pending_pay")
               .lt(TradeOrder::getCreateTime, expireTime)
               .eq(TradeOrder::getIsDeleted, 0);

        var expiredOrders = orderMapper.selectList(wrapper);
        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("[订单超时扫描] 发现 {} 个超时未付款订单待处理", expiredOrders.size());
        int count = 0;
        for (TradeOrder order : expiredOrders) {
            try {
                order.setStatus("cancelled");
                order.setBuyerCancel(0);  // 0=系统取消
                order.setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(order);
                count++;
                log.info("[订单超时取消] orderId={}, orderNo={}, 创建时间={}, 已超时{}小时",
                        order.getId(), order.getOrderNo(),
                        order.getCreateTime(), ORDER_TIMEOUT_HOURS);
            } catch (Exception e) {
                log.error("[订单超时取消失败] orderId={}, error={}",
                        order.getId(), e.getMessage());
            }
        }
        log.info("[订单超时扫描] 本次共取消 {} 个超时订单", count);
    }
}
