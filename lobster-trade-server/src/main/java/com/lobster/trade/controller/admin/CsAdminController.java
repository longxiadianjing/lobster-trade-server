package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.response.CsSessionVO;
import com.lobster.trade.service.CsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin-side CS session management (live chat customer service)
 */
@RestController
@RequestMapping("/api/admin/cs")
@RequiredArgsConstructor
public class CsAdminController {

    private final CsService csService;

    /**
     * List all CS sessions (admin view, no user restriction)
     */
    @GetMapping("/sessions")
    @RequirePermission(AdminPermission.CS_VIEW)
    public Result<IPage<CsSessionVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(csService.listForAdmin(keyword, status, page, size));
    }

    /**
     * Get single session detail (admin, bypass user check)
     */
    @GetMapping("/session/{id}")
    @RequirePermission(AdminPermission.CS_VIEW)
    public Result<CsSessionVO> get(@PathVariable Long id) {
        return Result.success(csService.getSessionAdmin(id));
    }

    /**
     * Admin sends a message to a CS session — accepts JSON body
     */
    @PostMapping("/message")
    @RequirePermission(AdminPermission.CS_HANDLE)
    public Result<CsSessionVO> sendMessage(@RequestBody CsAdminMessageRequest req) {
        return Result.success(csService.sendMessageAdmin(req.getSessionId(), req.getContent(),
            req.getMessageType(), req.getAttachmentUrl()));
    }

    @Data
    public static class CsAdminMessageRequest {
        private Long sessionId;
        private String content;
        private String messageType;
        private String attachmentUrl;
    }

    /**
     * Assign / take over a CS session as operator
     */
    @PostMapping("/assign")
    @RequirePermission(AdminPermission.CS_HANDLE)
    public Result<Void> assign(@RequestParam Long sessionId, @RequestParam Long operatorId, @RequestParam String operatorName) {
        csService.assignOperator(sessionId, operatorId, operatorName);
        return Result.success(null);
    }

    /**
     * Close a CS session
     */
    @PostMapping("/close/{sessionId}")
    @RequirePermission(AdminPermission.CS_HANDLE)
    public Result<Void> close(@PathVariable Long sessionId) {
        csService.closeSessionAdmin(sessionId);
        return Result.success(null);
    }

    /**
     * Stats for dashboard
     */
    @GetMapping("/stats")
    @RequirePermission(AdminPermission.CS_VIEW)
    public Result<Map<String, Object>> stats() {
        return Result.success(csService.getCsStats());
    }

}