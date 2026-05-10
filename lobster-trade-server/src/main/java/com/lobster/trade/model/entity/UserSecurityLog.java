package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_security_log")
public class UserSecurityLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 操作类型：PASSWORD_CHANGE/PAY_PASSWORD_SET/REAL_NAME/BIND_CARD/LOGIN */
    private String actionType;

    /** IP地址 */
    private String ipAddress;

    /** 设备信息 */
    private String deviceInfo;

    /** 操作详情 */
    private String detail;

    /** 操作结果：SUCCESS/FAILED */
    private String result;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
