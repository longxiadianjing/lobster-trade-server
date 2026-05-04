package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_transaction")
public class PaymentTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付单号 */
    private String paymentNo;

    /** 关联用户 */
    private Long userId;

    /** 关联订单ID（充值时为null，订单支付时为orderId） */
    private Long orderId;

    /** 支付类型：recharge-充值, order-订单支付 */
    private String paymentType;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付渠道：alipay-支付宝, wechat-微信, bankcard-银行卡 */
    private String channel;

    /** 状态：0-待支付, 1-支付成功, 2-支付失败, 3-已过期 */
    private Integer status;

    /** 第三方交易流水号（Mock） */
    private String transactionId;

    /** 支付成功时间 */
    private LocalDateTime paidTime;

    /** 过期时间（创建后10分钟过期） */
    private LocalDateTime expireTime;

    /** 错误信息 */
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    // ========== 常量定义 ==========

    /** 支付类型：充值 */
    public static final String TYPE_RECHARGE = "recharge";

    /** 支付类型：订单支付 */
    public static final String TYPE_ORDER = "order";

    /** 支付渠道：支付宝 */
    public static final String CHANNEL_ALIPAY = "alipay";

    /** 支付渠道：微信 */
    public static final String CHANNEL_WECHAT = "wechat";

    /** 支付渠道：银行卡 */
    public static final String CHANNEL_BANKCARD = "bankcard";

    /** 状态：待支付 */
    public static final int STATUS_PENDING = 0;

    /** 状态：支付成功 */
    public static final int STATUS_SUCCESS = 1;

    /** 状态：支付失败 */
    public static final int STATUS_FAILED = 2;

    /** 状态：已过期 */
    public static final int STATUS_EXPIRED = 3;
}