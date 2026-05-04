package com.lobster.trade.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private String tradeType;
    private Long productId;
    private String productTitle;
    private Long sellerId;
    private String sellerNickname;
    private Long buyerId;
    private String buyerNickname;
    private Long gameId;
    private String gameName;
    private BigDecimal orderAmount;
    private BigDecimal escrowAmount;
    private Integer escrowStatus;
    private Integer paymentStatus;
    private String status;
    private String boostRequirement;
    private LocalDateTime startTime;
    private LocalDateTime estimatedCompleteTime;
    private LocalDateTime submitTime;
    private LocalDateTime confirmTime;
    private LocalDateTime createTime;
    private Integer disputeStatus;    // 0=无 1=处理中 2=已处理
    private String disputeReason;
    private String disputeResult;
    private Boolean hasReviewed;
    // 买家对卖家的评价
    private String buyerReviewContent;
    private Integer buyerReviewRating;
    private LocalDateTime buyerReviewTime;
    // 卖家对买家的评价
    private String sellerReviewContent;
    private Integer sellerReviewRating;
    private LocalDateTime sellerReviewTime;
    // 当前用户发出的评价内容（用于提交后展示）
    private String myReviewContent;
    private Integer myReviewRating;
    // 优惠券折扣金额
    private BigDecimal couponDiscount;
}
