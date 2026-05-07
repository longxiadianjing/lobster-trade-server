package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.mapper.CouponMapper;
import com.lobster.trade.mapper.UserCouponMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.Coupon;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.entity.UserCoupon;
import com.lobster.trade.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserMapper userMapper;

    @Override
    public Map<String, Object> listCoupons(int page, int pageSize, String name, Integer status) {
        LambdaQueryWrapper<Coupon> countWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            countWrapper.like(Coupon::getName, name);
            wrapper.like(Coupon::getName, name);
        }
        if (status != null) {
            countWrapper.eq(Coupon::getStatus, status);
            wrapper.eq(Coupon::getStatus, status);
        }
        long total = couponMapper.selectCount(countWrapper);
        wrapper.orderByDesc(Coupon::getCreateTime);
        wrapper.last("LIMIT " + ((page - 1) * pageSize) + ", " + pageSize);
        List<Coupon> records = couponMapper.selectList(wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("total", total);
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCoupon(Coupon coupon) {
        coupon.setIssuedCount(0);
        coupon.setStatus(1);
        couponMapper.insert(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoupon(Coupon coupon) {
        if (coupon.getId() == null) throw new BusinessException("优惠券ID不能为空");
        couponMapper.updateById(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCoupon(Long id) {
        couponMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) throw new BusinessException("优惠券不存在");
        coupon.setStatus(coupon.getStatus() == 1 ? 0 : 1);
        couponMapper.updateById(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) throw new BusinessException("优惠券不存在");
        if (coupon.getStatus() != 1) throw new BusinessException("优惠券已禁用");
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new BusinessException("优惠券尚未开始领取");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new BusinessException("优惠券已过期");
        }
        if (coupon.getTotalCount() != null && coupon.getIssuedCount() >= coupon.getTotalCount()) {
            throw new BusinessException("优惠券已领完");
        }

        // 每人限领检查
        if (coupon.getPerUserLimit() != null && coupon.getPerUserLimit() > 0) {
            long owned = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getIsDeleted, 0)
            );
            if (owned >= coupon.getPerUserLimit()) {
                throw new BusinessException("该优惠券每人限领 " + coupon.getPerUserLimit() + " 张");
            }
        }

        // 发放
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setCouponName(coupon.getName());
        uc.setDiscountValue(coupon.getDiscountValue());
        uc.setReceiveTime(now);
        uc.setStatus(0);
        userCouponMapper.insert(uc);

        // 更新发行数量
        coupon.setIssuedCount(coupon.getIssuedCount() == null ? 1 : coupon.getIssuedCount() + 1);
        couponMapper.updateById(coupon);
    }

    @Override
    public List<Map<String, Object>> getMyCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId).eq(UserCoupon::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        wrapper.orderByDesc(UserCoupon::getReceiveTime);
        List<UserCoupon> list = userCouponMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : list) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", uc.getId());
            item.put("couponId", uc.getCouponId());
            item.put("couponName", uc.getCouponName());
            item.put("discountValue", uc.getDiscountValue());
            item.put("receiveTime", uc.getReceiveTime());
            item.put("useTime", uc.getUseTime());
            item.put("status", uc.getStatus());
            if (coupon != null) {
                item.put("minAmount", coupon.getMinAmount());
                item.put("discountRate", coupon.getDiscountRate());
                item.put("scope", coupon.getScope());
                item.put("endTime", coupon.getEndTime());
                item.put("type", coupon.getType());
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Coupon> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getStatus, 1)
               .le(Coupon::getEndTime, now.plusDays(30))
               .orderByDesc(Coupon::getCreateTime)
               .last("LIMIT 20");
        return couponMapper.selectList(wrapper);
    }
    private static final int BATCH_SIZE = 500;

    @Override
    public void distributeToAllUsers(Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) throw new BusinessException("优惠券不存在");
        if (coupon.getStatus() != 1) throw new BusinessException("优惠券未启用，无法发放");

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new BusinessException("优惠券尚未开始领取");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new BusinessException("优惠券已过期");
        }

        // 先查出用户总数，分批处理避免事务超时
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getIsDeleted, 0));
        int totalPages = (int) Math.ceil((double) totalUsers / BATCH_SIZE);
        int totalDistributed = 0;

        for (int page = 1; page <= totalPages; page++) {
            List<User> userBatch = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .eq(User::getIsDeleted, 0)
                    .last("LIMIT " + BATCH_SIZE + " OFFSET " + (page - 1) * BATCH_SIZE)
            );
            int batchDistributed = distributeBatch(coupon, userBatch, now);
            totalDistributed += batchDistributed;
        }

        // 更新发行数量
        coupon.setIssuedCount(coupon.getIssuedCount() == null ? totalDistributed : coupon.getIssuedCount() + totalDistributed);
        couponMapper.updateById(coupon);
    }

    @Transactional(rollbackFor = Exception.class)
    public int distributeBatch(Coupon coupon, List<User> userBatch, LocalDateTime now) {
        int count = 0;
        for (User user : userBatch) {
            if (coupon.getPerUserLimit() != null && coupon.getPerUserLimit() > 0) {
                long owned = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, user.getId())
                    .eq(UserCoupon::getCouponId, coupon.getId())
                    .eq(UserCoupon::getIsDeleted, 0)
                );
                if (owned >= coupon.getPerUserLimit()) continue;
            }
            UserCoupon uc = new UserCoupon();
            uc.setUserId(user.getId());
            uc.setCouponId(coupon.getId());
            uc.setCouponName(coupon.getName());
            uc.setDiscountValue(coupon.getDiscountValue());
            uc.setReceiveTime(now);
            uc.setStatus(0);
            userCouponMapper.insert(uc);
            count++;
        }
        return count;
    }

    @Override
    public List<Map<String, Object>> getAvailableCouponsForOrder(Long userId, BigDecimal orderAmount) {

        List<Map<String, Object>> myCoupons = getMyCoupons(userId, 0);
        List<Map<String, Object>> usable = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Map<String, Object> uc : myCoupons) {
            Coupon coupon = couponMapper.selectById((Long) uc.get("couponId"));
            if (coupon == null) continue;
            // 检查有效期
            if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
                uc.put("status", 2); // 已过期
                continue;
            }
            // 检查门槛
            if (coupon.getMinAmount() != null && orderAmount.compareTo(coupon.getMinAmount()) < 0) {
                continue;
            }
            // 检查范围（scope 2=商品购买）
            if (coupon.getScope() != null && coupon.getScope() != 0 && coupon.getScope() != 2) {
                continue;
            }
            usable.add(uc);
        }
        return usable;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userId, Long userCouponId, Long orderId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || uc.getIsDeleted() == 1) throw new BusinessException("优惠券不存在");
        if (!uc.getUserId().equals(userId)) throw new BusinessException("无权使用该优惠券");
        if (uc.getStatus() != 0) throw new BusinessException("优惠券不可用");
        uc.setStatus(1);
        uc.setUseTime(LocalDateTime.now());
        uc.setOrderId(orderId);
        userCouponMapper.updateById(uc);
    }
}
