package com.lobster.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.TradeReview;
import com.lobster.trade.service.JwtAuthService;
import com.lobster.trade.service.TradeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
class ReviewApiController {

    private final TradeReviewService reviewService;
    private final JwtAuthService jwtAuthService;

    private Long getCurrentUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtAuthService.getUserIdFromToken(token);
        }
        return null;
    }

    @PostMapping("/create")
    public Result<TradeReview> create(@RequestHeader("Authorization") String authHeader,
                                       @RequestParam Long orderId,
                                       @RequestParam Integer role,
                                       @RequestParam Integer rating,
                                       @RequestParam(required = false) String content,
                                       @RequestParam(required = false, defaultValue = "0") Integer isAnonymous) {
        Long userId = getCurrentUserId(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return Result.success(reviewService.createReview(userId, orderId, role, rating, content, isAnonymous));
    }

    @GetMapping("/user/{userId}")
    public Result<IPage<TradeReview>> getReviewsForUser(@PathVariable Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getReviewsForUser(userId, page, size));
    }

    @GetMapping("/profile/{userId}")
    public Result<Map<String, Object>> getUserProfile(@PathVariable Long userId) {
        return Result.success(reviewService.getUserProfile(userId));
    }

    @GetMapping("/product/{productId}")
    public Result<IPage<TradeReview>> getProductReviews(@PathVariable Long productId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getReviewsForProduct(productId, page, size));
    }

    @GetMapping("/seller/{sellerId}")
    public Result<IPage<TradeReview>> getSellerReviews(@PathVariable Long sellerId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getReviewsForUser(sellerId, page, size));
    }

    @PostMapping("/reply/{reviewId}")
    public Result<Void> replyReview(@PathVariable Long reviewId,
                                     @RequestHeader("Authorization") String authHeader,
                                     @RequestParam String replyContent) {
        Long userId = getCurrentUserId(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        reviewService.replyReview(reviewId, userId, replyContent);
        return Result.success(null);
    }

}