package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.SysNotificationMapper;
import com.lobster.trade.model.entity.SysNotification;
import com.lobster.trade.service.SysNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysNotificationServiceImpl implements SysNotificationService {

    private final SysNotificationMapper notificationMapper;

    @Override
    public IPage<SysNotification> listForAdmin(String keyword, String type, String status, int page, int size) {
        LambdaQueryWrapper<SysNotification> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) q.eq(SysNotification::getType, Integer.parseInt(type));
        if (StringUtils.hasText(status)) q.eq(SysNotification::getStatus, Integer.parseInt(status));
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(SysNotification::getTitle, keyword).or().like(SysNotification::getContent, keyword));
        }
        q.orderByDesc(SysNotification::getCreateTime);
        Page<SysNotification> p = new Page<>(page, size);
        return notificationMapper.selectPage(p, q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(SysNotification notification) {
        if (notification.getLevel() == null) notification.setLevel(3);
        if (notification.getStatus() == null) notification.setStatus(0);
        if (notification.getCreateTime() == null) notification.setCreateTime(LocalDateTime.now());
        if (notification.getUpdateTime() == null) notification.setUpdateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long id) {
        notificationMapper.deleteById(id);
    }

    @Override
    public long getUnreadCountAll() {
        return notificationMapper.selectCount(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getStatus, 0));
    }

    @Override
    public IPage<SysNotification> getMyNotifications(Long userId, String type, int page, int size) {
        LambdaQueryWrapper<SysNotification> q = new LambdaQueryWrapper<>();
        q.and(w -> w.eq(SysNotification::getUserId, userId).or().eq(SysNotification::getUserId, 0L));
        if (StringUtils.hasText(type)) q.eq(SysNotification::getType, Integer.parseInt(type));
        q.orderByDesc(SysNotification::getCreateTime);
        Page<SysNotification> p = new Page<>(page, size);
        return notificationMapper.selectPage(p, q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long notificationId) {
        SysNotification n = notificationMapper.selectById(notificationId);
        if (n == null || n.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "通知不存在");
        }
        if (!n.getUserId().equals(userId) && n.getUserId() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "无权操作");
        }
        n.setStatus(1);
        n.setReadTime(LocalDateTime.now());
        n.setUpdateTime(LocalDateTime.now());
        notificationMapper.updateById(n);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        SysNotification dummy = new SysNotification();
        dummy.setStatus(1);
        dummy.setReadTime(LocalDateTime.now());
        dummy.setUpdateTime(LocalDateTime.now());
        notificationMapper.update(dummy,
                new LambdaQueryWrapper<SysNotification>()
                        .and(w -> w.eq(SysNotification::getUserId, userId).or().eq(SysNotification::getUserId, 0L))
                        .eq(SysNotification::getStatus, 0));
    }

    @Override
    public long getMyUnreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<SysNotification>()
                .and(w -> w.eq(SysNotification::getUserId, userId).or().eq(SysNotification::getUserId, 0L))
                .eq(SysNotification::getStatus, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createForUser(Long userId, String title, String content, Integer type, String linkUrl) {
        SysNotification n = new SysNotification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setLevel(3);
        n.setStatus(0);
        n.setLinkUrl(linkUrl);
        n.setCreateTime(LocalDateTime.now());
        n.setUpdateTime(LocalDateTime.now());
        notificationMapper.insert(n);
    }
}