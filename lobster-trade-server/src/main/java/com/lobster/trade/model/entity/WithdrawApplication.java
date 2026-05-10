package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("withdraw_application")
public class WithdrawApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提现申请号 */
    private String applyNo;

    /** 用户ID */
    private Long userId;

    /** 申请金额 */
    private BigDecimal amount;

    /** 手续费 */
    private BigDecimal fee;

    /** 实到金额 */
    private BigDecimal actualAmount;

    /** 提现渠道：alipay/wechat/bank_card */
    private String channel;

    /** 账户信息（账号/卡号） */
    private String account;

    /** 账户姓名 */
    private String accountName;

    /** 状态：0=待审核 1=处理中 2=已打款 3=已拒绝 4=已取消 */
    private Integer status;

    /** 管理员备注/拒绝原因 */
    private String remark;

    /** 审核人ID */
    private Long auditorId;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 打款交易号 */
    private String transactionId;

    /** 成功时间 */
    private LocalDateTime successTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
