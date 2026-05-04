package com.lobster.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.model.entity.SysNotification;
import java.util.List;

public interface SysNotificationService {

    // Admin
    IPage<SysNotification> listForAdmin(String keyword, String type, String status, int page, int size);
    void createNotification(SysNotification notification);
    void deleteNotification(Long id);
    long getUnreadCountAll();

    // User
    IPage<SysNotification> getMyNotifications(Long userId, String type, int page, int size);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
    long getMyUnreadCount(Long userId);
    void createForUser(Long userId, String title, String content, Integer type, String linkUrl);
}