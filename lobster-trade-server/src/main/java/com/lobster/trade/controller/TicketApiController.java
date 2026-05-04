package com.lobster.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.ServiceTicket;
import com.lobster.trade.service.ServiceTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
class TicketApiController {

    private final ServiceTicketService ticketService;

    @GetMapping("/list")
    public Result<IPage<ServiceTicket>> getMyTickets(
            @RequestAttribute Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(ticketService.getMyTickets(userId, status, page, size));
    }

    @PostMapping("/create")
    public Result<ServiceTicket> create(@RequestAttribute Long userId, @RequestBody ServiceTicket ticket) {
        return Result.success(ticketService.createTicket(userId, ticket));
    }

    @PostMapping("/reply")
    public Result<Void> reply(@RequestAttribute Long userId,
                              @RequestParam Long ticketId,
                              @RequestParam String reply) {
        ticketService.reply(userId, ticketId, reply);
        return Result.success(null);
    }

    @PutMapping("/close")
    public Result<Void> close(@RequestAttribute Long userId,
                              @RequestParam Long ticketId,
                              @RequestParam(required = false) Integer satisfaction,
                              @RequestParam(required = false) String feedback) {
        ticketService.close(userId, ticketId, satisfaction, feedback);
        return Result.success(null);
    }
}