package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.entity.PlatformAnnouncement;
import com.lobster.trade.service.PlatformAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final PlatformAnnouncementService announcementService;

    @GetMapping("/list")
    @RequirePermission(AdminPermission.ANNOUNCEMENT_VIEW)
    public Result<IPage<PlatformAnnouncement>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(announcementService.listForAdmin(keyword, status, type, page, size));
    }

    @PostMapping("/create")
    @RequirePermission(AdminPermission.ANNOUNCEMENT_EDIT)
    public Result<Void> create(@RequestBody PlatformAnnouncement announcement) {
        announcementService.create(announcement);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RequirePermission(AdminPermission.ANNOUNCEMENT_EDIT)
    public Result<Void> update(@PathVariable Long id, @RequestBody PlatformAnnouncement announcement) {
        announcementService.update(id, announcement);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(AdminPermission.ANNOUNCEMENT_EDIT)
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/publish")
    @RequirePermission(AdminPermission.ANNOUNCEMENT_EDIT)
    public Result<Void> publish(@PathVariable Long id) {
        announcementService.publish(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/unpublish")
    @RequirePermission(AdminPermission.ANNOUNCEMENT_EDIT)
    public Result<Void> unpublish(@PathVariable Long id) {
        announcementService.unpublish(id);
        return Result.success(null);
    }
}