package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.request.AuditLogRequest;
import com.lobster.trade.model.response.AuditLogVO;
import com.lobster.trade.service.AdminAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AdminAuditService auditService;

    @GetMapping("/list")
    public Result<IPage<AuditLogVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endTime) {
        AuditLogRequest req = new AuditLogRequest();
        req.setAdminId(adminId);
        req.setAction(action);
        req.setEntityType(entityType);
        req.setStartTime(startTime);
        req.setEndTime(endTime);
        req.setPage(page);
        req.setSize(size);
        return Result.success(auditService.list(req));
    }
}