package com.lobster.trade.service;

import com.lobster.trade.model.entity.Coupon;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponService {

    /** 后台：分页查询优惠券 */
    Map<String, Object> listCoupons(int page, int pageSize, String name, Integer status);

    /** 后台：创建优惠券 */
    void createCoupon(Coupon coupon);

    /** 后台：更新优惠券 */
    void updateCoupon(Coupon coupon);

    /** 后台：删除优惠券 */
    void deleteCoupon(Long id);

    /** 后台：启用/禁用优惠券 */
    void toggleStatus(Long id);

    /** 前台：用户领取优惠券 */
    void receiveCoupon(Long userId, Long couponId);

    /** 前台：获取用户优惠券列表 */
    List<Map<String, Object>> getMyCoupons(Long userId, Integer status);

    /** 前台：获取当前可领取的优惠券列表 */
    List<Coupon> getAvailableCoupons();

    /** 管理员：向所有用户发放优惠券 */
    void distributeToAllUsers(Long couponId);

    /** 订单支付时：计算可用优惠券 */
    List<Map<String, Object>> getAvailableCouponsForOrder(Long userId, BigDecimal orderAmount);

    /** 使用优惠券（核销） */
    void useCoupon(Long userId, Long userCouponId, Long orderId);
}
