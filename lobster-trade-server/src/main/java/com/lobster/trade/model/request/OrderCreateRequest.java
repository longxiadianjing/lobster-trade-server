package com.lobster.trade.model.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OrderCreateRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    private String boostRequirement;  // 代练要求描述
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;      // wallet/alipay/wechat
    private Long couponId;             // 使用的优惠券ID（用户领用的user_coupon.id）
}
