package com.lobster.trade.model.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogRequest {
    private Long adminId;
    private String action;
    private String entityType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 20;
}