package com.lobster.trade.controller.admin;

import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @RequirePermission("DASHBOARD_VIEW")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.getOverview());
    }

    @GetMapping("/order-trend")
    @RequirePermission("DASHBOARD_VIEW")
    public Result<List<Map<String, Object>>> orderTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.getOrderTrend(days));
    }

    @GetMapping("/user-trend")
    @RequirePermission("DASHBOARD_VIEW")
    public Result<List<Map<String, Object>>> userTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.getUserTrend(days));
    }

    @GetMapping("/order-status")
    @RequirePermission("DASHBOARD_VIEW")
    public Result<Map<String, Long>> orderStatus() {
        return Result.success(statsService.getOrderStatusDistribution());
    }

    @GetMapping("/top-products")
    @RequirePermission("DASHBOARD_VIEW")
    public Result<List<Map<String, Object>>> topProducts(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.getTopProducts(limit));
    }
}
