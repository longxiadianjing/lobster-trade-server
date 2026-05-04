package com.lobster.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.model.entity.PlatformAnnouncement;
import java.util.List;

public interface PlatformAnnouncementService {

    IPage<PlatformAnnouncement> listForAdmin(String keyword, String status, String type, int page, int size);

    void create(PlatformAnnouncement announcement);

    void update(Long id, PlatformAnnouncement announcement);

    void delete(Long id);

    void publish(Long id);

    void unpublish(Long id);

    List<PlatformAnnouncement> getPublishedList();

    PlatformAnnouncement getById(Long id);
}