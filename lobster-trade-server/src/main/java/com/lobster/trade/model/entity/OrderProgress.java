package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_progress")
public class OrderProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private String orderNo;
    private Integer progressPercent;
    private String progressNote;
    private String screenshots;  // JSON数组
    private String evidenceType;
    private Integer sellerSubmit;
    private LocalDateTime sellerSubmitTime;
    private Integer buyerAck;
    private LocalDateTime buyerAckTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer isDeleted;
}
