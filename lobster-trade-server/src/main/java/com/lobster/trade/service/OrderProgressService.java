package com.lobster.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lobster.trade.model.entity.OrderProgress;

public interface OrderProgressService extends IService<OrderProgress> {

    /**
     * 提交/更新代练进度（卖家操作）
     */
    void updateProgress(Long orderId, Long sellerId, Integer percent, String note, String screenshots);

    /**
     * 获取订单进度记录
     */
    OrderProgress getByOrderId(Long orderId);

    /**
     * 买家确认进度（确认发货成果）
     */
    void buyerAck(Long orderId, Long buyerId, Integer ack);
}