package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.entity.TradeReview;
import com.lobster.trade.service.TradeReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final TradeReviewService reviewService;

    @GetMapping("/list")
    @RequirePermission(AdminPermission.REVIEW_VIEW)
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String rating,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            IPage<TradeReview> p = reviewService.listForAdmin(keyword, rating, page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("records", p.getRecords());
            result.put("total", p.getTotal());
            result.put("size", p.getSize());
            result.put("current", p.getCurrent());
            return Result.success(result);
        } catch (Exception e) {
            log.error("list review error", e);
            return Result.error(500, e.getMessage());
        }
    }

    @PutMapping("/hide")
    @RequirePermission(AdminPermission.REVIEW_EDIT)
    public Result<Void> hide(@RequestBody Map<String, Object> body) {
        Long reviewId = Long.valueOf(body.get("id").toString());
        Boolean hide = Boolean.valueOf(body.get("isHidden").toString());
        reviewService.hideReview(reviewId, hide);
        return Result.success(null);
    }
}
