package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private String tradeType;      // boost/accompany/escort/goods
    private Long productId;
    private String productTitle;
    private Long sellerId;
    private Long buyerId;
    private Long gameId;
    private Long categoryId;
    private BigDecimal orderAmount;
    private BigDecimal depositSeller;
    private BigDecimal depositBuyer;
    private BigDecimal commissionRate;
    private BigDecimal platformFee;
    private BigDecimal sellerReceived;
    private BigDecimal escrowAmount;
    private Integer escrowStatus;  // 1-未托管 2-已托管 3-已释放 4-已退款
    private Integer paymentStatus; // 0-未支付 1-已支付 2-已退款
    private LocalDateTime paymentTime;
    private String paymentMethod;
    private String status;         // pending_pay/paid/in_progress/submitted/confirmed/completed/disputed/cancelled
    private String boostRequirement;
    private LocalDateTime startTime;
    private LocalDateTime estimatedCompleteTime;
    private LocalDateTime actualCompleteTime;
    private LocalDateTime submitTime;
    private LocalDateTime confirmTime;
    private String deliveryImages;  // JSON数组
    private String deliveryRemark;
    private Integer buyerCancel;
    private Integer refundRequest;
    private String refundReason;
    private Integer disputeStatus;
    private String disputeReason;
    private String disputeResult;
    private LocalDateTime disputeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer isDeleted;
}
