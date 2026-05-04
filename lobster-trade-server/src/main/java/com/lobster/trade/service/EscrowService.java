package com.lobster.trade.service;

import com.lobster.trade.model.entity.TradeOrder;

public interface EscrowService {

    /**
     * 释放托管资金给卖家（订单完成后调用）
     * @param order 订单
     */
    void releaseEscrow(TradeOrder order);

    /**
     * 退款给买家（订单取消时调用）
     * @param order 订单
     */
    void refundEscrow(TradeOrder order);

    /**
     * 冻结买家资金（下单时调用）
     * @param order 订单
     */
    void freezeEscrow(TradeOrder order);
}