package com.lobster.trade.model.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderCreateRequest {
    private Long productId;
    private String boostRequirement;  // 代练要求描述
    private String paymentMethod;      // wallet/alipay/wechat
    private Long couponId;             // 使用的优惠券ID（用户领用的user_coupon.id）
}
