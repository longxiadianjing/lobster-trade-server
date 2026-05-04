package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet")
public class Wallet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;

    /** 是否设置支付密码：0-否，1-是 */
    private Integer passwordSet;

    private String password;

    private String alipayAccount;
    private String alipayName;
    private String wechatOpenid;
    private String bankCardNo;
    private String bankName;
    private String bankUsername;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
