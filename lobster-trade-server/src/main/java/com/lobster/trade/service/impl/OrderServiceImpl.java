package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.*;
import com.lobster.trade.model.entity.*;
import com.lobster.trade.model.request.OrderCreateRequest;
import com.lobster.trade.model.response.OrderDetailVO;
import com.lobster.trade.service.EscrowService;
import com.lobster.trade.service.OrderService;
import com.lobster.trade.service.SysNotificationService;
import com.lobster.trade.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final ProductMapper productMapper;
    private final GameCategoryMapper gameCategoryMapper;
    private final UserMapper userMapper;
    private final TradeReviewMapper tradeReviewMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final com.lobster.trade.service.ImService imService;
    private final EscrowService escrowService;
    private final SysNotificationService sysNotificationService;

    @Override
    @Transactional
    public OrderDetailVO create(OrderCreateRequest req, Long buyerId) {
        Product product = productMapper.selectById(req.getProductId());
        if (product == null || product.getIsDeleted() == 1 || product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在或已下架");
        }
        if (product.getStock() == null || product.getStock() < 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "该商品已售罄，请选择其他商品");
        }
        if (buyerId.equals(product.getSellerId())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "不能购买自己的商品");
        }

        BigDecimal orderAmount = product.getPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;

        // 优惠券处理
        if (req.getCouponId() != null) {
            UserCoupon uc = userCouponMapper.selectById(req.getCouponId());
            if (uc == null || uc.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "优惠券不存在");
            }
            if (!uc.getUserId().equals(buyerId)) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "无权使用该优惠券");
            }
            if (uc.getStatus() != 0) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "优惠券不可用");
            }
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon == null || coupon.getStatus() != 1) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "优惠券已禁用");
            }
            // 检查有效期
            LocalDateTime now = LocalDateTime.now();
            if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "优惠券已过期");
            }
            if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "优惠券尚未开始");
            }
            // 检查门槛（商品购买范围：scope=0全场 或 scope=2商品购买）
            if (coupon.getScope() != null && coupon.getScope() != 0 && coupon.getScope() != 2) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "该优惠券不适用于商品购买");
            }
            if (coupon.getMinAmount() != null && orderAmount.compareTo(coupon.getMinAmount()) < 0) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "订单金额不满足优惠券使用条件（需满" + coupon.getMinAmount() + "元）");
            }
            // 计算优惠金额
            if (coupon.getType() == 1) {
                discountAmount = coupon.getDiscountValue() != null ? coupon.getDiscountValue() : BigDecimal.ZERO;
            } else if (coupon.getType() == 2) {
                if (coupon.getDiscountRate() != null) {
                    discountAmount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate()));
                }
            }
            if (discountAmount.compareTo(orderAmount) > 0) {
                discountAmount = orderAmount;
            }
            // 立即标记优惠券为已使用
            uc.setStatus(1);
            uc.setUseTime(now);
            userCouponMapper.updateById(uc);
        }

        final BigDecimal finalDiscount = discountAmount;
        final BigDecimal finalOrderAmount = orderAmount.subtract(discountAmount).compareTo(BigDecimal.ZERO) > 0 ? orderAmount.subtract(discountAmount) : BigDecimal.ZERO;

        TradeOrder order = new TradeOrder();
        order.setOrderNo("ORD" + SnowflakeIdUtil.generateOrderNo());
        order.setTradeType(product.getProductType());
        order.setProductId(product.getId());
        order.setProductTitle(product.getTitle());
        order.setSellerId(product.getSellerId());
        order.setBuyerId(buyerId);
        order.setGameId(product.getGameId());
        order.setCategoryId(product.getCategoryId());
        order.setOrderAmount(finalOrderAmount);
        order.setEscrowAmount(finalOrderAmount);
        order.setCommissionRate(new BigDecimal("0.0500"));
        order.setPlatformFee(finalOrderAmount.multiply(new BigDecimal("0.0500")));
        order.setSellerReceived(finalOrderAmount.subtract(finalOrderAmount.multiply(new BigDecimal("0.0500"))));
        order.setEscrowStatus(1);
        order.setPaymentStatus(0);
        order.setStatus("pending_pay");
        order.setBoostRequirement(req.getBoostRequirement());
        tradeOrderMapper.insert(order);

        // 原子扣减库存（乐观锁）
        int updated = productMapper.decrementStock(product.getId(), 1);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "库存不足，下单失败");
        }

        // 创建IM会话
        imService.createSessionForOrder(order.getId(), buyerId, product.getSellerId());
        // 通知卖家：有新订单
        sysNotificationService.createForUser(product.getSellerId(),
                "📋 您有新订单",
                "买家购买了您的商品【" + product.getTitle() + "】，请等待买家付款。订单号：" + order.getOrderNo(),
                2, "/order/detail/" + order.getId());

        OrderDetailVO vo = toVO(order);
        vo.setCouponDiscount(finalDiscount);
        return vo;
    }

    @Override
    public Page<OrderDetailVO> listByBuyer(Long buyerId, String status, int page, int size) {
        return queryOrders(buyerId, "buyer_id", status, page, size);
    }

    @Override
    public Page<OrderDetailVO> listBySeller(Long sellerId, String status, int page, int size) {
        return queryOrders(sellerId, "seller_id", status, page, size);
    }

    private Page<OrderDetailVO> queryOrders(Long userId, String role, String status, int page, int size) {
        Page<TradeOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<TradeOrder>()
            .eq(TradeOrder::getIsDeleted, 0);
        if ("buyer_id".equals(role)) wrapper.eq(TradeOrder::getBuyerId, userId);
        else wrapper.eq(TradeOrder::getSellerId, userId);
        if (status != null && !status.isEmpty()) wrapper.eq(TradeOrder::getStatus, status);
        wrapper.orderByDesc(TradeOrder::getCreateTime);

        Page<TradeOrder> result = tradeOrderMapper.selectPage(pageParam, wrapper);
        List<OrderDetailVO> records = new ArrayList<>();
        for (TradeOrder o : result.getRecords()) records.add(toVO(o));
        Page<OrderDetailVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public OrderDetailVO getDetail(Long orderId, Long userId) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看");
        }
        OrderDetailVO vo = toVO(order);

        // hasReviewed
        LambdaQueryWrapper<TradeReview> reviewQ = new LambdaQueryWrapper<>();
        reviewQ.eq(TradeReview::getOrderId, orderId).eq(TradeReview::getReviewerId, userId);
        vo.setHasReviewed(tradeReviewMapper.selectCount(reviewQ) > 0);

        // buyer review (role=1)
        LambdaQueryWrapper<TradeReview> buyerReviewQ = new LambdaQueryWrapper<>();
        buyerReviewQ.eq(TradeReview::getOrderId, orderId).eq(TradeReview::getRole, 1);
        buyerReviewQ.orderByDesc(TradeReview::getCreateTime).last("LIMIT 1");
        java.util.List<TradeReview> buyerReviewList = tradeReviewMapper.selectList(buyerReviewQ);
        TradeReview buyerReview = buyerReviewList.isEmpty() ? null : buyerReviewList.get(0);
        if (buyerReview != null) {
            vo.setBuyerReviewContent(buyerReview.getContent());
            vo.setBuyerReviewRating(buyerReview.getRating());
            vo.setBuyerReviewTime(buyerReview.getCreateTime());
            if (order.getBuyerId().equals(userId)) {
                vo.setMyReviewContent(buyerReview.getContent());
                vo.setMyReviewRating(buyerReview.getRating());
            }
        }

        // seller review (role=2)
        LambdaQueryWrapper<TradeReview> sellerReviewQ = new LambdaQueryWrapper<>();
        sellerReviewQ.eq(TradeReview::getOrderId, orderId).eq(TradeReview::getRole, 2);
        sellerReviewQ.orderByDesc(TradeReview::getCreateTime).last("LIMIT 1");
        java.util.List<TradeReview> sellerReviewList = tradeReviewMapper.selectList(sellerReviewQ);
        TradeReview sellerReview = sellerReviewList.isEmpty() ? null : sellerReviewList.get(0);
        if (sellerReview != null) {
            vo.setSellerReviewContent(sellerReview.getContent());
            vo.setSellerReviewRating(sellerReview.getRating());
            vo.setSellerReviewTime(sellerReview.getCreateTime());
            if (order.getSellerId().equals(userId)) {
                vo.setMyReviewContent(sellerReview.getContent());
                vo.setMyReviewRating(sellerReview.getRating());
            }
        }

        return vo;
    }

    @Override
    @Transactional
    public void pay(Long orderId, Long userId, String paymentMethod) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以付款");
        }
        if (!"pending_pay".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "当前状态不支持支付");
        }
        order.setPaymentStatus(1);
        order.setPaymentTime(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);
        order.setStatus("paid");
        tradeOrderMapper.updateById(order);
        // 冻结托管资金
        escrowService.freezeEscrow(order);
        // 通知卖家：买家已付款
        sysNotificationService.createForUser(order.getSellerId(),
                "💰 买家已付款",
                "您发布的商品【" + order.getProductTitle() + "】有买家已付款，请尽快发货。订单号：" + order.getOrderNo(),
                2, "/order/detail/" + order.getId());
    }

    @Override
    @Transactional
    public void submitDelivery(Long orderId, Long sellerId, String deliveryImages, String deliveryRemark) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有卖家可以提交发货");
        }
        if (!"paid".equals(order.getStatus()) && !"in_progress".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "当前状态不支持提交");
        }
        order.setDeliveryImages(deliveryImages);
        order.setDeliveryRemark(deliveryRemark);
        order.setSubmitTime(LocalDateTime.now());
        order.setStatus("submitted");
        tradeOrderMapper.updateById(order);
        // 通知买家：商品已发货
        sysNotificationService.createForUser(order.getBuyerId(),
                "📦 卖家已发货",
                "您购买的商品【" + order.getProductTitle() + "】卖家已发货，请及时确认收货。订单号：" + order.getOrderNo(),
                2, "/order/detail/" + order.getId());
    }

    @Override
    @Transactional
    public void confirmDelivery(Long orderId, Long buyerId) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以确认");
        }
        if (!"submitted".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "当前状态不支持确认");
        }
        order.setConfirmTime(LocalDateTime.now());
        order.setStatus("completed");
        tradeOrderMapper.updateById(order);
        // 释放托管资金给卖家
        escrowService.releaseEscrow(order);
        // 通知卖家：交易完成
        sysNotificationService.createForUser(order.getSellerId(),
                "✅ 订单已完成",
                "商品【" + order.getProductTitle() + "】交易已完成，款项已到账。订单号：" + order.getOrderNo(),
                2, "/order/detail/" + order.getId());
        // 通知买家：交易完成
        sysNotificationService.createForUser(order.getBuyerId(),
                "✅ 订单已完成",
                "您购买的商品【" + order.getProductTitle() + "】交易已完成，欢迎评价。订单号：" + order.getOrderNo(),
                2, "/order/detail/" + order.getId());
    }

    @Override
    @Transactional
    public void cancel(Long orderId, Long userId, String reason) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限取消");
        }
        if (!"pending_pay".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "当前状态不支持取消");
        }
        order.setStatus("cancelled");
        if (order.getBuyerId().equals(userId)) {
            order.setRefundRequest(1);
            order.setRefundReason(reason);
            // 退款给买家
            escrowService.refundEscrow(order);
            // 通知买家：订单已取消并退款
            sysNotificationService.createForUser(order.getBuyerId(),
                    "❌ 订单已取消",
                    "您的订单【" + order.getProductTitle() + "】已取消，款项将退还至您的钱包。订单号：" + order.getOrderNo(),
                    2, "/order/detail/" + order.getId());
        }
        tradeOrderMapper.updateById(order);

        // 原子还原库存
        productMapper.incrementStock(order.getProductId(), 1);
    }

    private OrderDetailVO toVO(TradeOrder o) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setTradeType(o.getTradeType());
        vo.setProductId(o.getProductId());
        vo.setProductTitle(o.getProductTitle());
        vo.setSellerId(o.getSellerId());
        vo.setBuyerId(o.getBuyerId());
        vo.setGameId(o.getGameId());
        vo.setOrderAmount(o.getOrderAmount());
        vo.setEscrowAmount(o.getEscrowAmount());
        vo.setEscrowStatus(o.getEscrowStatus());
        vo.setPaymentStatus(o.getPaymentStatus());
        vo.setStatus(o.getStatus());
        vo.setBoostRequirement(o.getBoostRequirement());
        vo.setStartTime(o.getStartTime());
        vo.setEstimatedCompleteTime(o.getEstimatedCompleteTime());
        vo.setSubmitTime(o.getSubmitTime());
        vo.setConfirmTime(o.getConfirmTime());
        vo.setCreateTime(o.getCreateTime());
        vo.setDisputeStatus(o.getDisputeStatus());
        vo.setDisputeReason(o.getDisputeReason());
        vo.setDisputeResult(o.getDisputeResult());
        User su = userMapper.selectById(o.getSellerId());
        if (su != null) vo.setSellerNickname(su.getNickname());
        User bu = userMapper.selectById(o.getBuyerId());
        if (bu != null) vo.setBuyerNickname(bu.getNickname());
        GameCategory g = gameCategoryMapper.selectById(o.getGameId());
        if (g != null) vo.setGameName(g.getGameName());
        vo.setCouponDiscount(BigDecimal.ZERO);
        return vo;
    }

    private OrderDetailVO buildOrderDetailVO(TradeOrder o) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setTradeType(o.getTradeType());
        vo.setProductId(o.getProductId());
        vo.setProductTitle(o.getProductTitle());
        vo.setSellerId(o.getSellerId());
        vo.setBuyerId(o.getBuyerId());
        vo.setGameId(o.getGameId());
        vo.setOrderAmount(o.getOrderAmount());
        vo.setEscrowAmount(o.getEscrowAmount());
        vo.setEscrowStatus(o.getEscrowStatus());
        vo.setPaymentStatus(o.getPaymentStatus());
        vo.setStatus(o.getStatus());
        vo.setBoostRequirement(o.getBoostRequirement());
        vo.setStartTime(o.getStartTime());
        vo.setEstimatedCompleteTime(o.getEstimatedCompleteTime());
        vo.setSubmitTime(o.getSubmitTime());
        vo.setConfirmTime(o.getConfirmTime());
        vo.setCreateTime(o.getCreateTime());
        vo.setDisputeStatus(o.getDisputeStatus());
        vo.setDisputeReason(o.getDisputeReason());
        vo.setDisputeResult(o.getDisputeResult());
        User su = userMapper.selectById(o.getSellerId());
        if (su != null) vo.setSellerNickname(su.getNickname());
        User bu = userMapper.selectById(o.getBuyerId());
        if (bu != null) vo.setBuyerNickname(bu.getNickname());
        GameCategory g = gameCategoryMapper.selectById(o.getGameId());
        if (g != null) vo.setGameName(g.getGameName());
        vo.setCouponDiscount(BigDecimal.ZERO);
        return vo;
    }

    @Override
    public OrderDetailVO getAdminDetail(Long orderId) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        return buildOrderDetailVO(order);
    }

    @Override
    public void adminUpdateStatus(Long orderId, String status, String reason) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        order.setStatus(status);
        tradeOrderMapper.updateById(order);
    }
}
