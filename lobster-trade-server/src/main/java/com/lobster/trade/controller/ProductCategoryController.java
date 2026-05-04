package com.lobster.trade.controller;

import com.lobster.trade.model.entity.ProductCategory;
import com.lobster.trade.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @GetMapping("/game/{gameId}")
    public com.lobster.trade.model.response.ApiResponse<List<ProductCategory>> getByGameId(@PathVariable Long gameId) {
        return com.lobster.trade.model.response.ApiResponse.success(categoryService.getByGameId(gameId));
    }
}