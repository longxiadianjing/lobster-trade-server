package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.entity.HotSearchWord;
import com.lobster.trade.service.HotSearchWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/hot-search")
@RequiredArgsConstructor
public class HotSearchWordController {

    private final HotSearchWordService hotSearchWordService;

    @GetMapping("/list")
    @RequirePermission(AdminPermission.HOTSEARCH_VIEW)
    public Result<IPage<HotSearchWord>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(hotSearchWordService.listWords(keyword, page, size));
    }

    @PostMapping("/create")
    @RequirePermission(AdminPermission.HOTSEARCH_EDIT)
    public Result<Void> create(@RequestBody HotSearchWord word) {
        hotSearchWordService.create(word);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @RequirePermission(AdminPermission.HOTSEARCH_EDIT)
    public Result<Void> update(@PathVariable Long id, @RequestBody HotSearchWord word) {
        hotSearchWordService.update(id, word);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(AdminPermission.HOTSEARCH_EDIT)
    public Result<Void> delete(@PathVariable Long id) {
        hotSearchWordService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/toggle")
    @RequirePermission(AdminPermission.HOTSEARCH_EDIT)
    public Result<Void> toggle(@PathVariable Long id) {
        hotSearchWordService.toggleStatus(id);
        return Result.success(null);
    }
}

@RestController
@RequestMapping("/api/hot-search")
@RequiredArgsConstructor
class HotSearchWordApiController {

    private final HotSearchWordService hotSearchWordService;

    @GetMapping("/words")
    public Result<List<HotSearchWord>> getEnabledWords() {
        return Result.success(hotSearchWordService.getEnabledWords());
    }
}
