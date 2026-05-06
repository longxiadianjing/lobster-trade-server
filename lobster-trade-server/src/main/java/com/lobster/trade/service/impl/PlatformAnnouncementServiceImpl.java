package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.PlatformAnnouncementMapper;
import com.lobster.trade.model.entity.PlatformAnnouncement;
import com.lobster.trade.service.PlatformAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformAnnouncementServiceImpl implements PlatformAnnouncementService {

    private final PlatformAnnouncementMapper announcementMapper;

    @Override
    public IPage<PlatformAnnouncement> listForAdmin(String keyword, String status, String type, int page, int size) {
        LambdaQueryWrapper<PlatformAnnouncement> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) q.eq(PlatformAnnouncement::getStatus, Integer.parseInt(status));
        if (StringUtils.hasText(type)) q.eq(PlatformAnnouncement::getType, Integer.parseInt(type));
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(PlatformAnnouncement::getTitle, keyword).or().like(PlatformAnnouncement::getContent, keyword));
        }
        q.orderByDesc(PlatformAnnouncement::getCreateTime);
        Page<PlatformAnnouncement> p = new Page<>(page, size);
        return announcementMapper.selectPage(p, q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(PlatformAnnouncement announcement) {
        if (announcement.getPriority() == null) announcement.setPriority(3);
        if (announcement.getStatus() == null) announcement.setStatus(0);
        if (announcement.getViewCount() == null) announcement.setViewCount(0);
        if (announcement.getStatus() != null && announcement.getStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.insert(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PlatformAnnouncement announcement) {
        PlatformAnnouncement existing = announcementMapper.selectById(id);
        if (existing == null || existing.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "公告不存在");
        }
        if (announcement.getTitle() != null) existing.setTitle(announcement.getTitle());
        if (announcement.getContent() != null) existing.setContent(announcement.getContent());
        if (announcement.getType() != null) existing.setType(announcement.getType());
        if (announcement.getPriority() != null) existing.setPriority(announcement.getPriority());
        if (announcement.getStatus() != null) existing.setStatus(announcement.getStatus());
        if (announcement.getPublishTime() != null) existing.setPublishTime(announcement.getPublishTime());
        if (announcement.getEndTime() != null) existing.setEndTime(announcement.getEndTime());
        existing.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        PlatformAnnouncement a = announcementMapper.selectById(id);
        if (a == null || a.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "公告不存在");
        }
        a.setStatus(1);
        a.setPublishTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(a);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublish(Long id) {
        PlatformAnnouncement a = announcementMapper.selectById(id);
        if (a == null || a.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "公告不存在");
        }
        a.setStatus(0);
        a.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(a);
    }

    @Override
    public List<PlatformAnnouncement> getPublishedList() {
        LambdaQueryWrapper<PlatformAnnouncement> q = new LambdaQueryWrapper<>();
        q.eq(PlatformAnnouncement::getStatus, 1);
        q.orderByDesc(PlatformAnnouncement::getPublishTime);
        return announcementMapper.selectList(q);
    }

    @Override
    public PlatformAnnouncement getById(Long id) {
        return announcementMapper.selectById(id);
    }
}