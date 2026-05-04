package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_transaction")
public class WalletTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transNo;
    private Long userId;

    /** 类型：1-收入，2-支出，3-冻结，4-解冻，5-退款 */
    private Integer type;

    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal frozenBefore;
    private BigDecimal frozenAfter;

    /** 来源：order / recharge / withdraw / commission / refund */
    private String source;

    private Long sourceId;
    private String sourceNo;

    /** 状态：1-成功，2-失败，3-处理中 */
    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}
