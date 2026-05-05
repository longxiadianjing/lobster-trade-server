package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.entity.ProductRecommendSlot;
import com.lobster.trade.service.ProductRecommendSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/recommend-slot")
@RequiredArgsConstructor
public class ProductRecommendSlotController {

    private final ProductRecommendSlotService slotService;

    @GetMapping("/list")
    @RequirePermission(AdminPermission.RECSLOT_VIEW)
    public Result<Page<ProductRecommendSlot>> list(
            @RequestParam(required = false) String slotKey,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(slotService.listSlots(slotKey, page, size));
    }

    @PostMapping("/create")
    @RequirePermission(AdminPermission.RECSLOT_EDIT)
    public Result<Void> create(@RequestBody ProductRecommendSlot slot) {
        slotService.create(slot);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RequirePermission(AdminPermission.RECSLOT_EDIT)
    public Result<Void> update(@PathVariable Long id, @RequestBody ProductRecommendSlot slot) {
        slotService.update(id, slot);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(AdminPermission.RECSLOT_EDIT)
    public Result<Void> delete(@PathVariable Long id) {
        slotService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/toggle")
    @RequirePermission(AdminPermission.RECSLOT_EDIT)
    public Result<Void> toggle(@PathVariable Long id) {
        slotService.toggleStatus(id);
        return Result.success(null);
    }

    @GetMapping("/product/{productId}")
    public Result<?> getByProduct(@PathVariable Long productId) {
        return Result.success(slotService.getSlotsByProductId(productId));
    }
}
