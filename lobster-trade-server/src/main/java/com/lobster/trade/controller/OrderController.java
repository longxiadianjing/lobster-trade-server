package com.lobster.trade.controller;

import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.model.response.OrderDetailVO;
import com.lobster.trade.service.JwtAuthService;
import com.lobster.trade.service.OrderService;
import com.lobster.trade.model.request.OrderCreateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtAuthService jwtAuthService;

    @PostMapping
    public ApiResponse<OrderDetailVO> create(@RequestBody OrderCreateRequest req,
                                           @RequestHeader("Authorization") String token) {
        Long buyerId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return ApiResponse.success(orderService.create(req, buyerId));
    }

    @GetMapping("/buyer/list")
    public ApiResponse<Page<OrderDetailVO>> buyerList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String token) {
        Long buyerId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return ApiResponse.success(orderService.listByBuyer(buyerId, status, page, size));
    }

    @GetMapping("/seller/list")
    public ApiResponse<Page<OrderDetailVO>> sellerList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String token) {
        Long sellerId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return ApiResponse.success(orderService.listBySeller(sellerId, status, page, size));
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<OrderDetailVO> detail(@PathVariable Long id,
                                             @RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return ApiResponse.success(orderService.getDetail(id, userId));
    }

    @PostMapping("/pay/{id}")
    public ApiResponse<Void> pay(@PathVariable Long id,
                                 @RequestParam String paymentMethod,
                                 @RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        orderService.pay(id, userId, paymentMethod);
        return ApiResponse.success(null);
    }

    @PostMapping("/submit/{id}")
    public ApiResponse<Void> submitDelivery(@PathVariable Long id,
                                             @RequestParam(required = false) String deliveryImages,
                                             @RequestParam(required = false) String deliveryRemark,
                                             @RequestHeader("Authorization") String token) {
        Long sellerId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        orderService.submitDelivery(id, sellerId, deliveryImages, deliveryRemark);
        return ApiResponse.success(null);
    }

    @PostMapping("/confirm/{id}")
    public ApiResponse<Void> confirmDelivery(@PathVariable Long id,
                                              @RequestHeader("Authorization") String token) {
        Long buyerId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        orderService.confirmDelivery(id, buyerId);
        return ApiResponse.success(null);
    }

    @PostMapping("/cancel/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                     @RequestParam String reason,
                                     @RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        orderService.cancel(id, userId, reason);
        return ApiResponse.success(null);
    }
}
