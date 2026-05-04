package com.lobster.trade.controller;

import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.model.response.OrderSimpleVO;
import com.lobster.trade.model.response.ProductDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.mapper.ProductMapper;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.mapper.GameCategoryMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.GameCategory;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.response.ProductDetailVO;
import com.lobster.trade.service.JwtAuthService;
import com.lobster.trade.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsApiController {

    private final TradeOrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final GameCategoryMapper gameCategoryMapper;
    private final UserMapper userMapper;
    private final ProductService productService;

    /** 获取卖家统计数据 */
    @GetMapping("/seller/{sellerId}")
    public ApiResponse<Map<String, Object>> getSellerStats(@PathVariable Long sellerId) {
        User user = userMapper.selectById(sellerId);
        if (user == null) {
            return ApiResponse.success(new java.util.HashMap<>());
        }
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalTradeCount", user.getTotalTradeCount() != null ? user.getTotalTradeCount() : 0);
        stats.put("totalTradeAmount", user.getTotalTradeAmount() != null ? user.getTotalTradeAmount() : java.math.BigDecimal.ZERO);
        stats.put("reputationScore", user.getReputationScore() != null ? user.getReputationScore() : java.math.BigDecimal.valueOf(5.0));
        stats.put("productCount", productMapper.selectCount(new LambdaQueryWrapper<com.lobster.trade.model.entity.Product>()
                .eq(com.lobster.trade.model.entity.Product::getSellerId, sellerId)
                .eq(com.lobster.trade.model.entity.Product::getStatus, 1)));
        return ApiResponse.success(stats);
    }

    /** 公开的卖家基础信息（无需认证） */
    @GetMapping("/seller-public/{sellerId}")
    public ApiResponse<Map<String, Object>> getSellerPublicInfo(@PathVariable Long sellerId) {
        User user = userMapper.selectById(sellerId);
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        Map<String, Object> info = new java.util.HashMap<>();
        info.put("id", user.getId());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("realNameStatus", user.getRealNameStatus());
        info.put("isVerified", user.getIsVerified());
        info.put("reputationScore", user.getReputationScore());
        info.put("totalTradeCount", user.getTotalTradeCount());
        info.put("totalTradeAmount", user.getTotalTradeAmount());
        info.put("userLevel", user.getUserLevel());
        info.put("createTime", user.getCreateTime());
        return ApiResponse.success(info);
    }

    /** 本周热销TOP10（按成交量） */
    @GetMapping("/hot-products")
    public ApiResponse<List<ProductDetailVO>> hotProducts() {
        List<TradeOrder> orders = orderMapper.selectList(
            new LambdaQueryWrapper<TradeOrder>()
                .in(TradeOrder::getStatus, List.of("paid", "in_progress", "submitted", "confirmed", "completed"))
                .orderByDesc(TradeOrder::getCreateTime)
                .last("LIMIT 200")
        );

        java.util.Map<Long, java.util.List<TradeOrder>> byProduct = orders.stream()
                .collect(Collectors.groupingBy(TradeOrder::getProductId));

        List<Long> topIds = byProduct.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(10)
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toList());

        List<ProductDetailVO> result = new ArrayList<>();
        for (Long pid : topIds) {
            try {
                result.add(productService.getDetail(pid));
            } catch (Exception ignored) {}
        }
        return ApiResponse.success(result);
    }

    /** 最近成交动态 */
    @GetMapping("/recent-orders")
    public ApiResponse<List<OrderSimpleVO>> recentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<TradeOrder>()
                .in(TradeOrder::getStatus, List.of("paid", "in_progress", "submitted", "confirmed", "completed"))
                .orderByDesc(TradeOrder::getCreateTime)
                .last("LIMIT " + limit);
        List<TradeOrder> orders = orderMapper.selectList(wrapper);

        List<OrderSimpleVO> result = orders.stream().map(o -> {
            OrderSimpleVO vo = new OrderSimpleVO();
            vo.setId(o.getId());
            vo.setProductTitle(o.getProductTitle());
            vo.setOrderAmount(o.getOrderAmount());
            vo.setCreateTime(o.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        return ApiResponse.success(result);
    }
}
