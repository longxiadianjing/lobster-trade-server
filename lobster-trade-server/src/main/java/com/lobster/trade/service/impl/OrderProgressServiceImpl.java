package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.OrderProgressMapper;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.model.entity.OrderProgress;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.service.OrderProgressService;
import com.lobster.trade.service.SysNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderProgressServiceImpl extends ServiceImpl<OrderProgressMapper, OrderProgress> implements OrderProgressService {

    private final OrderProgressMapper progressMapper;
    private final TradeOrderMapper orderMapper;
    private final SysNotificationService notificationService;

    public OrderProgressServiceImpl(OrderProgressMapper progressMapper, TradeOrderMapper orderMapper,
                                    SysNotificationService notificationService) {
        this.progressMapper = progressMapper;
        this.orderMapper = orderMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long orderId, Long sellerId, Integer percent, String note, String screenshots) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有卖家可提交进度");
        }
        String validStatus = order.getStatus();
        if (!"paid".equals(validStatus) && !"in_progress".equals(validStatus) && !"submitted".equals(validStatus)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "当前状态不支持更新进度");
        }

        OrderProgress p = progressMapper.selectOne(
            new LambdaQueryWrapper<OrderProgress>()
                .eq(OrderProgress::getOrderId, orderId)
                .eq(OrderProgress::getIsDeleted, 0)
        );

        if (p == null) {
            p = new OrderProgress();
            p.setOrderId(orderId);
            p.setOrderNo(order.getOrderNo());
            p.setProgressPercent(percent);
            p.setProgressNote(note);
            p.setScreenshots(screenshots);
            p.setSellerSubmit(1);
            p.setSellerSubmitTime(LocalDateTime.now());
            p.setBuyerAck(0);
            p.setCreateTime(LocalDateTime.now());
            progressMapper.insert(p);

            // 如果是从 paid 变为进行中，更新订单状态
            if ("paid".equals(order.getStatus())) {
                order.setStatus("in_progress");
                order.setStartTime(LocalDateTime.now());
                orderMapper.updateById(order);
            }
        } else {
            p.setProgressPercent(percent);
            p.setProgressNote(note);
            p.setScreenshots(screenshots);
            p.setSellerSubmit(1);
            p.setSellerSubmitTime(LocalDateTime.now());
            progressMapper.updateById(p);

            // 当进度达到100%时，通知买家验收
            if (percent != null && percent == 100) {
                notificationService.createForUser(order.getBuyerId(),
                    "代练订单已完成",
                    "您的代练订单【" + order.getProductTitle() + "】已完成，请验收确认。",
                    3, null);
            }
        }
    }

    @Override
    public OrderProgress getByOrderId(Long orderId) {
        return progressMapper.selectOne(
            new LambdaQueryWrapper<OrderProgress>()
                .eq(OrderProgress::getOrderId, orderId)
                .eq(OrderProgress::getIsDeleted, 0)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyerAck(Long orderId, Long buyerId, Integer ack) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可确认进度");
        }

        OrderProgress p = progressMapper.selectOne(
            new LambdaQueryWrapper<OrderProgress>()
                .eq(OrderProgress::getOrderId, orderId)
                .eq(OrderProgress::getIsDeleted, 0)
        );
        if (p == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "进度记录不存在");
        }

        p.setBuyerAck(ack != null ? ack : 1);
        p.setBuyerAckTime(LocalDateTime.now());
        progressMapper.updateById(p);
    }
}