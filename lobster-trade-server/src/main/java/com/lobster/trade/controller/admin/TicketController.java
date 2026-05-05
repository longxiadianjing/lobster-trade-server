package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.entity.ServiceTicket;
import com.lobster.trade.service.ServiceTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final ServiceTicketService ticketService;

    @GetMapping("/list")
    @RequirePermission(AdminPermission.TICKET_VIEW)
    public Result<IPage<ServiceTicket>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(ticketService.listForAdmin(keyword, status, category, page, size));
    }

    @PostMapping("/assign")
    @RequirePermission(AdminPermission.TICKET_HANDLE)
    public Result<Void> assign(@RequestParam Long ticketId, @RequestParam Long handlerId, @RequestParam String handlerName) {
        ticketService.assignHandler(ticketId, handlerId, handlerName);
        return Result.success(null);
    }

    @PutMapping("/handle")
    @RequirePermission(AdminPermission.TICKET_HANDLE)
    public Result<Void> handle(@RequestParam Long ticketId,
                               @RequestParam(required = false) String reply,
                               @RequestParam(required = false) Integer status) {
        ticketService.handle(ticketId, reply, status);
        return Result.success(null);
    }

    @PutMapping("/close/{ticketId}")
    @RequirePermission(AdminPermission.TICKET_HANDLE)
    public Result<Void> close(@PathVariable Long ticketId) {
        ticketService.closeTicket(ticketId);
        return Result.success(null);
    }

    @GetMapping("/stats")
    @RequirePermission(AdminPermission.TICKET_VIEW)
    public Result<Map<String, Object>> stats() {
        return Result.success(ticketService.getStats());
    }
}