package com.lobster.trade.service.impl;

import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.request.DisputeRequest;
import com.lobster.trade.service.DisputeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final TradeOrderMapper orderMapper;

    @Override
    @Transactional
    public void createDispute(Long userId, DisputeRequest req) {
        createDispute(userId, String.valueOf(req.getOrderId()), req.getReason(), null, req.getEvidence());
    }

    @Override
    @Transactional
    public void createDispute(Long userId, String orderIdStr, String reason, String description, String images) {
        if (orderIdStr == null || orderIdStr.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单ID不能为空");
        }
        Long orderId;
        try {
            orderId = Long.parseLong(orderIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "无效的订单ID");
        }
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权发起仲裁");
        }

        if (!"paid".equals(order.getStatus()) && !"submitted".equals(order.getStatus()) && !"confirmed".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "当前状态不支持发起仲裁");
        }

        if (order.getDisputeStatus() != null && order.getDisputeStatus() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "该订单已有进行中的仲裁");
        }

        order.setStatus("disputed");
        order.setDisputeStatus(1);
        String fullReason = reason;
        if (description != null && !description.isEmpty()) {
            fullReason = reason + "：" + description;
        }
        if (images != null && !images.isEmpty()) {
            fullReason = fullReason + " [证据: " + images + "]";
        }
        order.setDisputeReason(fullReason);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void resolveDispute(Long adminId, Long orderId, String result) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (order.getDisputeStatus() == null || order.getDisputeStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "该订单没有进行中的仲裁");
        }

        order.setDisputeStatus(2);
        order.setDisputeResult(result);
        order.setDisputeTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        if (result.contains("退款")) {
            order.setEscrowStatus(3); // 已退款
            order.setStatus("cancelled");
        } else if (result.contains("放款") || result.contains("完成")) {
            order.setEscrowStatus(2); // 已释放
            order.setSellerReceived(order.getEscrowAmount());
            order.setStatus("completed");
        }

        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancelDispute(Long userId, Long orderId) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权取消仲裁");
        }
        if (order.getDisputeStatus() == null || order.getDisputeStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "该订单没有进行中的仲裁");
        }

        order.setDisputeStatus(0);
        order.setDisputeReason(null);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public long countPending() {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getDisputeStatus, 1);
        return orderMapper.selectCount(wrapper);
    }

    @Override
    public long countTotal() {
        return orderMapper.selectCount(null);
    }

    @Override
    public Map<String, Object> getDisputeDetail(Long orderId) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("productTitle", order.getProductTitle());
        map.put("orderAmount", order.getOrderAmount());
        map.put("escrowAmount", order.getEscrowAmount());
        map.put("buyerId", order.getBuyerId());
        map.put("sellerId", order.getSellerId());
        map.put("status", order.getStatus());
        map.put("disputeStatus", order.getDisputeStatus());
        map.put("disputeReason", order.getDisputeReason());
        map.put("disputeResult", order.getDisputeResult());
        map.put("disputeTime", order.getDisputeTime());
        map.put("createTime", order.getCreateTime());
        return map;
    }
}
