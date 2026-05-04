package com.lobster.trade.controller;

import com.lobster.trade.common.BaseContext;
import com.lobster.trade.common.Result;
import com.lobster.trade.model.response.SellerStatsResponse;
import com.lobster.trade.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/stats")
@RequiredArgsConstructor
public class SellerStatsController {

    private final OrderService orderService;

    @GetMapping("/summary")
    public Result<SellerStatsResponse> getSellerStats() {
        Long sellerId = BaseContext.getCurrentId();
        return Result.success(orderService.getSellerStats(sellerId));
    }
}
