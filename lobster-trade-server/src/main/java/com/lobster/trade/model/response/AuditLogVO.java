package com.lobster.trade.model.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogVO {
    private Long id;
    private Long adminId;
    private String adminUsername;
    private String action;
    private String entityType;
    private Long entityId;
    private String detail;
    private String ip;
    private LocalDateTime createTime;
}