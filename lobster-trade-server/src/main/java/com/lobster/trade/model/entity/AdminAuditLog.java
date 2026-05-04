package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_audit_log")
public class AdminAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;
    private String adminUsername;

    /** 操作类型 */
    private String action;

    /** 目标类型 */
    private String entityType;

    /** 目标ID */
    private Long entityId;

    /** 操作详情 */
    private String detail;

    /** IP地址 */
    private String ip;

    /** 浏览器UA */
    private String userAgent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
