package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 优惠券名称 */
    private String name;

    /** 优惠券描述 */
    private String description;

    /** 优惠类型：1=满减 2=折扣 3=充值赠送 */
    private Integer type;

    /** 满减门槛金额（type=1时必填） */
    private BigDecimal minAmount;

    /** 优惠金额/折扣率 */
    private BigDecimal discountValue;

    /** 折扣率（type=2时为折扣，如0.9=9折） */
    private BigDecimal discountRate;

    /** 充值赠送类型：充值多少送多少（如充100送10） */
    private BigDecimal rechargeAmount;
    private BigDecimal giftAmount;

    /** 发行总量 */
    private Integer totalCount;

    /** 已发放数量 */
    private Integer issuedCount;

    /** 每人限领数量 */
    private Integer perUserLimit;

    /** 使用门槛：0=无限制 */
    private BigDecimal useMinAmount;

    /** 有效期开始 */
    private LocalDateTime startTime;

    /** 有效期结束 */
    private LocalDateTime endTime;

    /** 状态：0=禁用 1=启用 */
    private Integer status;

    /** 适用场景：0=全场 1=充值 2=商品购买 */
    private Integer scope;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
