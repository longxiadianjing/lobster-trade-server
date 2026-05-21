package com.lobster.trade.controller;

import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.model.entity.OrderProgress;
import com.lobster.trade.service.OrderProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/order/progress")
@RequiredArgsConstructor
public class OrderProgressController {

    private final OrderProgressService progressService;

    /**
     * 获取订单进度（GET /api/order/progress/{orderId}）
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderProgress> getProgress(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderProgress p = progressService.getByOrderId(orderId);
        return ApiResponse.success(p);
    }

    /**
     * 更新代练进度（POST /api/order/progress/{orderId}）
     */
    @PostMapping("/{orderId}")
    public ApiResponse<Void> updateProgress(
            @PathVariable Long orderId,
            @RequestParam Integer percent,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String screenshots,
            HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        progressService.updateProgress(orderId, sellerId, percent, note, screenshots);
        return ApiResponse.success(null);
    }

    /**
     * 买家确认进度（POST /api/order/progress/{orderId}/ack）
     */
    @PostMapping("/{orderId}/ack")
    public ApiResponse<Void> buyerAck(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "1") Integer ack,
            HttpServletRequest request) {
        Long buyerId = (Long) request.getAttribute("userId");
        progressService.buyerAck(orderId, buyerId, ack);
        return ApiResponse.success(null);
    }
}