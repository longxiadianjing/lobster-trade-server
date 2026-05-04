package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.SysNotification;
import com.lobster.trade.service.SysNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final SysNotificationService notificationService;

    @GetMapping("/list")
    public Result<IPage<SysNotification>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(notificationService.listForAdmin(keyword, type, status, page, size));
    }

    @PostMapping("/create")
    public Result<Void> create(@RequestBody SysNotification notification) {
        notificationService.createNotification(notification);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success(null);
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.getUnreadCountAll());
    }
}