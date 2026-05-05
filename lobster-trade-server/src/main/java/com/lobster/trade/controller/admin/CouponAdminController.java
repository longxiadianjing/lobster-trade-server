package com.lobster.trade.controller.admin;

import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.entity.Coupon;
import com.lobster.trade.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupon")
@RequiredArgsConstructor
public class CouponAdminController {

    private final CouponService couponService;

    @GetMapping("/list")
    @RequirePermission(AdminPermission.COUPON_VIEW)
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.success(couponService.listCoupons(page, pageSize, name, status));
    }

    @PostMapping
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> create(@RequestBody Coupon coupon) {
        couponService.createCoupon(coupon);
        return Result.success(null);
    }

    @PutMapping
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> update(@RequestBody Coupon coupon) {
        couponService.updateCoupon(coupon);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> delete(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/toggle")
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> toggle(@PathVariable Long id) {
        couponService.toggleStatus(id);
        return Result.success(null);
    }

    @PostMapping("/distribute")
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> distribute(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("userId")).longValue();
        Long couponId = ((Number) params.get("couponId")).longValue();
        couponService.receiveCoupon(userId, couponId);
        return Result.success(null);
    }

    @PostMapping("/distribute-all")
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> distributeAll(@RequestBody Map<String, Object> params) {
        Long couponId = ((Number) params.get("couponId")).longValue();
        couponService.distributeToAllUsers(couponId);
        return Result.success(null);
    }
}
