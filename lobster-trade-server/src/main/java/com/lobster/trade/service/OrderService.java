package com.lobster.trade.service;

import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.request.OrderCreateRequest;
import com.lobster.trade.model.response.OrderDetailVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface OrderService {
    OrderDetailVO create(OrderCreateRequest req, Long buyerId);
    Page<OrderDetailVO> listByBuyer(Long buyerId, String status, int page, int size);
    Page<OrderDetailVO> listBySeller(Long sellerId, String status, int page, int size);
    OrderDetailVO getDetail(Long orderId, Long userId);
    void pay(Long orderId, Long userId, String paymentMethod);
    void submitDelivery(Long orderId, Long sellerId, String deliveryImages, String deliveryRemark);
    void confirmDelivery(Long orderId, Long buyerId);
    void cancel(Long orderId, Long userId, String reason);

    /** 管理员获取订单详情（ bypass 用户权限） */
    OrderDetailVO getAdminDetail(Long orderId);

    /** 管理员手动更新订单状态 */
    void adminUpdateStatus(Long orderId, String status, String reason);
}
