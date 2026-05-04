package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_real_name")
public class UserRealName {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    private String idCard;

    private String idCardFront; // 身份证正面图片URL

    private String idCardBack;  // 身份证反面图片URL

    private Integer status; // 0=审核中 1=已通过 2=未通过 3=已撤回

    private String rejectReason;

    private String aliyunVerifyToken; // 阿里云实人认证token

    private String aliyunVerifyResult; // 阿里云返回结果JSON

    private LocalDateTime verifyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}