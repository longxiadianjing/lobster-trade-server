package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("trade_review")
public class TradeReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long reviewerId; // 评价方

    private Long reviewedId; // 被评价方

    private Integer role; // 1=买家评价卖家 2=卖家评价买家

    private Integer rating; // 1-5分

    private String content; // 评价内容

    private Integer isAnonymous; // 0=公开 1=匿名

    private Integer isHidden; // 管理员隐藏

    private String replyContent;  // 卖家回复

    private Long replierId;       // 回复人ID

    private LocalDateTime replyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}