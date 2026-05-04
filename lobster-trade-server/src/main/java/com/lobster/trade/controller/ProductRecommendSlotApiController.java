package com.lobster.trade.controller;

import com.lobster.trade.model.entity.Product;
import com.lobster.trade.service.ProductRecommendSlotService;
import com.lobster.trade.model.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend-slot")
@RequiredArgsConstructor
public class ProductRecommendSlotApiController {

    private final ProductRecommendSlotService slotService;

    /**
     * 获取当前生效的推荐位商品
     * GET /api/recommend-slot/active?slotKey=home_featured
     */
    @GetMapping("/active")
    public ApiResponse<List<Product>> getActiveSlots(@RequestParam String slotKey) {
        return ApiResponse.success(slotService.getActiveSlots(slotKey));
    }

    /**
     * 获取多个推荐位
     * GET /api/recommend-slot/batch?slotKeys=home_featured,home_banner
     */
    @GetMapping("/batch")
    public ApiResponse<?> getBatchSlots(@RequestParam String slotKeys) {
        List<String> keys = List.of(slotKeys.split(","));
        java.util.Map<String, List<Product>> result = new java.util.LinkedHashMap<>();
        for (String key : keys) {
            result.put(key.trim(), slotService.getActiveSlots(key.trim()));
        }
        return ApiResponse.success(result);
    }
}
