package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.TradeReviewMapper;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.TradeReview;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.service.TradeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TradeReviewServiceImpl implements TradeReviewService {

    private final TradeReviewMapper reviewMapper;
    private final TradeOrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeReview createReview(Long userId, Long orderId, Integer role, Integer rating, String content, Integer isAnonymous) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!"completed".equals(order.getStatus()) && !"confirmed".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单未完成，无法评价");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权评价此订单");
        }

        Long targetId = role == 1 ? order.getSellerId() : order.getBuyerId();
        LambdaQueryWrapper<TradeReview> existingQ = new LambdaQueryWrapper<TradeReview>()
                .eq(TradeReview::getOrderId, orderId)
                .eq(TradeReview::getReviewerId, userId)
                .eq(TradeReview::getRole, role);
        if (reviewMapper.selectCount(existingQ) > 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "您已评价过该订单");
        }

        TradeReview review = new TradeReview();
        review.setOrderId(orderId);
        review.setReviewerId(userId);
        review.setReviewedId(targetId);
        review.setRole(role);
        review.setRating(rating);
        review.setContent(content);
        review.setIsAnonymous(isAnonymous != null ? isAnonymous : 0);
        review.setIsHidden(0);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        reviewMapper.insert(review);

        updateSellerReputation(targetId);
        return review;
    }

    @Override
    public IPage<TradeReview> getReviewsForUser(Long userId, int page, int size) {
        LambdaQueryWrapper<TradeReview> q = new LambdaQueryWrapper<>();
        q.eq(TradeReview::getReviewedId, userId)
                .eq(TradeReview::getIsHidden, 0)
                .orderByDesc(TradeReview::getCreateTime);
        return reviewMapper.selectPage(new Page<>(page, size), q);
    }

    @Override
    public Map<String, Object> getUserProfile(Long userId) {
        Map<String, Object> profile = new HashMap<>();
        LambdaQueryWrapper<TradeReview> baseQ = new LambdaQueryWrapper<TradeReview>()
                .eq(TradeReview::getReviewedId, userId)
                .eq(TradeReview::getIsHidden, 0);

        long total = reviewMapper.selectCount(baseQ);
        List<TradeReview> rows = reviewMapper.selectList(baseQ.select(TradeReview::getRating));
        double avgRating = rows.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(TradeReview::getRating)
                .average().orElse(0.0);

        profile.put("totalReviews", total);
        profile.put("avgRating", Math.round(avgRating * 10) / 10.0);
        profile.put("fiveStar", reviewMapper.selectCount(new LambdaQueryWrapper<TradeReview>().eq(TradeReview::getReviewedId, userId).eq(TradeReview::getRating, 5).eq(TradeReview::getIsHidden, 0)));
        profile.put("fourStar", reviewMapper.selectCount(new LambdaQueryWrapper<TradeReview>().eq(TradeReview::getReviewedId, userId).eq(TradeReview::getRating, 4).eq(TradeReview::getIsHidden, 0)));
        profile.put("threeStar", reviewMapper.selectCount(new LambdaQueryWrapper<TradeReview>().eq(TradeReview::getReviewedId, userId).eq(TradeReview::getRating, 3).eq(TradeReview::getIsHidden, 0)));
        profile.put("twoStar", reviewMapper.selectCount(new LambdaQueryWrapper<TradeReview>().eq(TradeReview::getReviewedId, userId).eq(TradeReview::getRating, 2).eq(TradeReview::getIsHidden, 0)));
        profile.put("oneStar", reviewMapper.selectCount(new LambdaQueryWrapper<TradeReview>().eq(TradeReview::getReviewedId, userId).eq(TradeReview::getRating, 1).eq(TradeReview::getIsHidden, 0)));
        // 添加官方认证标识
        User user = userMapper.selectById(userId);
        if (user != null) {
            profile.put("isVerified", user.getIsVerified());
        }
        return profile;
    }

    @Override
    public IPage<TradeReview> listForAdmin(String keyword, String rating, int page, int size) {
        LambdaQueryWrapper<TradeReview> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(rating)) q.eq(TradeReview::getRating, Integer.parseInt(rating));
        if (StringUtils.hasText(keyword)) q.like(TradeReview::getContent, keyword);
        q.orderByDesc(TradeReview::getCreateTime);
        return reviewMapper.selectPage(new Page<>(page, size), q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hideReview(Long reviewId, boolean hide) {
        TradeReview r = reviewMapper.selectById(reviewId);
        if (r == null) throw new BusinessException(ErrorCode.PARAM_INVALID, "评价不存在");
        Long reviewedId = r.getReviewedId();
        r.setIsHidden(hide ? 1 : 0);
        r.setUpdateTime(LocalDateTime.now());
        reviewMapper.updateById(r);
        // 隐藏/取消隐藏后重新计算卖家信誉
        updateSellerReputation(reviewedId);
    }

    private void updateSellerReputation(Long sellerId) {
        LambdaQueryWrapper<TradeReview> q = new LambdaQueryWrapper<>();
        q.eq(TradeReview::getReviewedId, sellerId).eq(TradeReview::getIsHidden, 0);
        List<TradeReview> reviews = reviewMapper.selectList(q);
        if (reviews == null || reviews.isEmpty()) return;
        double avgRating = reviews.stream()
                .mapToInt(r -> r.getRating() != null ? r.getRating() : 0)
                .average().orElse(0.0);
        User seller = userMapper.selectById(sellerId);
        if (seller != null) {
            seller.setReputationScore(BigDecimal.valueOf(avgRating));
            seller.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(seller);
        }
    }

    @Override
    public IPage<TradeReview> getReviewsForProduct(Long productId, int page, int size) {
        LambdaQueryWrapper<TradeOrder> orderQ = new LambdaQueryWrapper<>();
        orderQ.eq(TradeOrder::getProductId, productId);
        List<TradeOrder> orders = orderMapper.selectList(orderQ);
        if (orders == null || orders.isEmpty()) {
            return new Page<>(page, size);
        }
        List<Long> orderIds = orders.stream().map(TradeOrder::getId).toList();
        LambdaQueryWrapper<TradeReview> q = new LambdaQueryWrapper<>();
        q.in(TradeReview::getOrderId, orderIds)
                .eq(TradeReview::getIsHidden, 0)
                .orderByDesc(TradeReview::getCreateTime);
        return reviewMapper.selectPage(new Page<>(page, size), q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(Long reviewId, Long replierId, String replyContent) {
        if (reviewId == null || replierId == null || replyContent == null || replyContent.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "参数错误");
        }
        TradeReview review = reviewMapper.selectById(reviewId);
        if (review == null || review.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "评价不存在");
        }
        // 只有被评价方（卖家）才能回复
        if (!review.getReviewedId().equals(replierId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权回复此评价");
        }
        review.setReplyContent(replyContent);
        review.setReplierId(replierId);
        review.setReplyTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

}