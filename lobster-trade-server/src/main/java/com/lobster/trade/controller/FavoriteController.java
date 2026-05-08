package com.lobster.trade.controller;

import com.lobster.trade.common.BaseContext;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.model.response.ProductDetailVO;
import com.lobster.trade.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     * POST /api/favorite/add?productId=123
     */
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestParam Long productId) {
        Long userId = BaseContext.getCurrentId();
        favoriteService.addFavorite(userId, productId);
        return ApiResponse.success(null);
    }

    /**
     * 取消收藏
     * DELETE /api/favorite/remove?productId=123
     */
    @DeleteMapping("/remove")
    public ApiResponse<Void> remove(@RequestParam Long productId) {
        Long userId = BaseContext.getCurrentId();
        favoriteService.removeFavorite(userId, productId);
        return ApiResponse.success(null);
    }

    /**
     * 我的收藏列表
     * GET /api/favorite/list
     */
    @GetMapping("/list")
    public ApiResponse<List<ProductDetailVO>> list() {
        Long userId = BaseContext.getCurrentId();
        return ApiResponse.success(favoriteService.getMyFavorites(userId));
    }

    /**
     * 检查是否已收藏
     * GET /api/favorite/check?productId=123
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> check(@RequestParam Long productId) {
        Long userId = BaseContext.getCurrentId();
        return ApiResponse.success(favoriteService.isFavorited(userId, productId));
    }
}