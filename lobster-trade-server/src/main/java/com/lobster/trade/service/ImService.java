package com.lobster.trade.service;

import com.lobster.trade.model.request.ImSendMessageRequest;
import com.lobster.trade.model.response.ImSessionVO;

import java.util.List;

public interface ImService {

    /**
     * 获取当前用户所有IM会话列表
     */
    List<ImSessionVO> getMySessions(Long userId);

    /**
     * 根据会话ID获取IM会话详情（含最近消息）
     */
    ImSessionVO getSession(Long userId, Long sessionId);

    /**
     * 根据订单ID获取/创建IM会话
     */
    ImSessionVO getOrCreateSession(Long userId, Long orderId);

    /**
     * 发送消息
     */
    ImSessionVO sendMessage(Long userId, ImSendMessageRequest req);

    /**
     * 标记会话已读
     */
    void markRead(Long userId, Long sessionId);

    /**
     * 创建订单时自动创建IM会话（内部调用）
     */
    void createSessionForOrder(Long orderId, Long buyerId, Long sellerId);

    /**
     * 根据商品ID获取或创建IM会话（买卖双方直接沟通，无需订单）
     */
    ImSessionVO getOrCreateSessionByProduct(Long userId, Long productId);
}
