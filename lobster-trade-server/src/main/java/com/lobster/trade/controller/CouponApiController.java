package com.lobster.trade.controller;

import com.lobster.trade.model.entity.Coupon;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponApiController {

    private final CouponService couponService;

    /**
     * 领取优惠券
     * POST /api/coupon/receive
     * Body: { couponId }
     */
    @PostMapping("/receive")
    public ApiResponse<Void> receive(
            @RequestBody Map<String, Long> body,
            @RequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);
        couponService.receiveCoupon(userId, body.get("couponId"));
        return ApiResponse.success(null);
    }

    /**
     * 我的优惠券列表
     * GET /api/coupon/my?status=0
     */
    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> myCoupons(
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);
        return ApiResponse.success(couponService.getMyCoupons(userId, status));
    }

    /**
     * 可领取的优惠券列表
     * GET /api/coupon/available
     */
    @GetMapping("/available")
    public ApiResponse<List<Coupon>> available() {
        return ApiResponse.success(couponService.getAvailableCoupons());
    }

    /**
     * 订单可用的优惠券
     * GET /api/coupon/order/usable?amount=100
     */
    @GetMapping("/order/usable")
    public ApiResponse<List<Map<String, Object>>> usableCoupons(
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);
        return ApiResponse.success(couponService.getAvailableCouponsForOrder(userId, amount));
    }

    private Long extractUserId(String auth) {
        try {
            String token = auth.substring(7);
            Map<String, Object> payload = com.lobster.trade.util.JwtUtil.verifyToken(token);
            Object uid = payload.get("userId");
            if (uid instanceof Integer) return ((Integer) uid).longValue();
            if (uid instanceof Long) return (Long) uid;
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
