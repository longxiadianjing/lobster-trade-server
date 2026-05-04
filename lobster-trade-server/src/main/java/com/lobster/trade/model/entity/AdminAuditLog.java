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

    private String action; // 操作类型

    private String entityType; // 操作的实体类型

    private Long entityId; // 操作的实体ID

    private String detail; // 操作详情

    private String ip;

    private String userAgent;

    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}