package com.lobster.trade.controller;

import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.model.response.ProductDetailVO;
import com.lobster.trade.service.JwtAuthService;
import com.lobster.trade.service.ProductService;
import com.lobster.trade.model.request.ProductPublishRequest;
import com.lobster.trade.model.entity.Product;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final JwtAuthService jwtAuthService;

    @PostMapping
    public ApiResponse<Long> publish(@RequestBody ProductPublishRequest req,
                                   @RequestHeader("Authorization") String token) {
        Long sellerId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return ApiResponse.success(productService.publish(req, sellerId));
    }

    @GetMapping("/list")
    public ApiResponse<Page<ProductDetailVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String productType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.list(keyword, gameId, productType, page, size));
    }

    @GetMapping("/my")
    public ApiResponse<List<Product>> my(@RequestHeader("Authorization") String token) {
        Long sellerId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return ApiResponse.success(productService.getMyProducts(sellerId));
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<ProductDetailVO> detail(@PathVariable Long id) {
        productService.increaseView(id);
        return ApiResponse.success(productService.getDetail(id));
    }

    @GetMapping("/search")
    public ApiResponse<Page<ProductDetailVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String productType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.search(keyword, gameId, productType, page, size));
    }
}
