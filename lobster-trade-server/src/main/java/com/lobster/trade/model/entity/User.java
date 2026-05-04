package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;

    private String password;

    private String payPassword;

    private String realName;
    private String idCard;
    private String idCardFront;
    private String idCardBack;

    /** 实名状态：0-未实名，1-已实名，2-审核中，3-未通过 */
    private Integer realNameStatus;

    /** 用户等级：1-普通，2-铜牌，3-银牌，4-金牌 */
    private Integer userLevel;

    /** 信誉评分 1-5 */
    private BigDecimal reputationScore;

    /** 累计交易次数 */
    private Integer totalTradeCount;

    /** 累计交易金额 */
    private BigDecimal totalTradeAmount;

    /** 钱包余额（元） */
    private BigDecimal balance;

    /** 冻结金额（元） */
    private BigDecimal frozenBalance;

    /** 账号状态：1-正常，2-封禁，3-冻结 */
    private Integer status;

    /** 官方认证标识：0-未认证，1-已认证 */
    private Integer isVerified;

    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private String registerIp;
    private String registerSource;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
