package com.lobster.trade.controller;

import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.PlatformAnnouncement;
import com.lobster.trade.service.PlatformAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementApiController {

    private final PlatformAnnouncementService announcementService;

    @GetMapping("/list")
    public Result<List<PlatformAnnouncement>> getPublishedList() {
        return Result.success(announcementService.getPublishedList());
    }

    @GetMapping("/{id}")
    public Result<PlatformAnnouncement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }
}