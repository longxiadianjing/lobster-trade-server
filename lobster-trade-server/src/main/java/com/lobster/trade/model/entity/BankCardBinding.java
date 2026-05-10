package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bank_card_binding")
public class BankCardBinding {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 银行名称 */
    private String bankName;

    /** 开户支行 */
    private String bankBranch;

    /** 卡号（加密存储） */
    private String cardNumber;

    /** 卡号后4位（展示用） */
    private String cardNumberLast4;

    /** 持卡人姓名 */
    private String holderName;

    /** 身份证号（加密存储） */
    private String idCardNumber;

    /** 身份证后4位 */
    private String idCardNumberLast4;

    /** 银行预留手机号 */
    private String phone;

    /** 状态：0=待验证 1=已绑定 2=已解绑 */
    private Integer status;

    /** 绑定时间 */
    private LocalDateTime bindTime;

    /** 解绑时间 */
    private LocalDateTime unbindTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
