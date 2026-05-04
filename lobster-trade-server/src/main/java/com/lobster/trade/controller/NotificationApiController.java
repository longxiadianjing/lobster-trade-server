package com.lobster.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.common.Result;
import com.lobster.trade.service.SysNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
class NotificationApiController {

    private final SysNotificationService notificationService;

    @GetMapping("/list")
    public Result<IPage> getMyNotifications(
            @RequestAttribute Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(notificationService.getMyNotifications(userId, type, page, size));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@RequestAttribute Long userId, @PathVariable Long id) {
        notificationService.markAsRead(userId, id);
        return Result.success(null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestAttribute Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success(null);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestAttribute Long userId) {
        return Result.success(notificationService.getMyUnreadCount(userId));
    }
}