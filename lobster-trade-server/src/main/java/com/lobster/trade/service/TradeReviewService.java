package com.lobster.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.model.entity.TradeReview;
import java.util.Map;

public interface TradeReviewService {

    TradeReview createReview(Long userId, Long orderId, Integer role, Integer rating, String content, Integer isAnonymous);

    IPage<TradeReview> getReviewsForUser(Long userId, int page, int size);

    Map<String, Object> getUserProfile(Long userId);

    IPage<TradeReview> listForAdmin(String keyword, String rating, int page, int size);

    void hideReview(Long reviewId, boolean hide);


    // Get reviews for a product (via order -> productId join)
    IPage<TradeReview> getReviewsForProduct(Long productId, int page, int size);

    // Reply to a review
    void replyReview(Long reviewId, Long replierId, String replyContent);

}