package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("escrow_transaction")
public class EscrowTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 托管流水号 */
    private String escrowNo;

    /** 关联订单ID */
    private Long orderId;

    /** 关联订单号 */
    private Long orderNo;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /** 托管金额 */
    private BigDecimal amount;

    /** 状态：PENDING/FROZEN/RELEASED/REFUNDED */
    private String status;

    /** 操作类型：FROZEN/RELEASE/REFUND/CANCEL */
    private String action;

    /** 操作前余额 */
    private BigDecimal balanceBefore;

    /** 操作后余额 */
    private BigDecimal balanceAfter;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
