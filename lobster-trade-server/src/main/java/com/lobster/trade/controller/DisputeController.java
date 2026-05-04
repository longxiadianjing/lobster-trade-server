package com.lobster.trade.controller;

import com.lobster.trade.common.Result;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.service.DisputeService;
import com.lobster.trade.service.JwtAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/dispute")
@RequiredArgsConstructor
public class DisputeController {

    @Resource
    private DisputeService disputeService;

    @Resource
    private JwtAuthService jwtAuthService;

    @Resource
    private TradeOrderMapper orderMapper;

    /**
     * 用户提交仲裁申请
     */
    @PostMapping
    public Result<Void> submit(@RequestHeader(value = "Authorization", required = false) String token,
                               @RequestBody Map<String, String> body) {
        String orderIdStr = body.get("orderId");
        String reason = body.get("reason");
        String description = body.get("description");
        String images = body.get("images");

        if (!StringUtils.hasText(orderIdStr)) {
            return Result.error("订单ID不能为空");
        }
        if (!StringUtils.hasText(reason)) {
            return Result.error("请选择仲裁原因");
        }

        Long userId = (token != null) ? jwtAuthService.getUserIdFromToken(token.replace("Bearer ", "")) : null;
        if (userId == null) {
            return Result.error("请先登录");
        }

        disputeService.createDispute(userId, orderIdStr, reason, description, images);
        return Result.success(null);
    }

    /**
     * 查询仲裁详情
     */
    @GetMapping("/{orderId}")
    public Result<Map<String, Object>> detail(@RequestHeader(value = "Authorization", required = false) String token,
                                              @PathVariable Long orderId) {
        Long userId = (token != null) ? jwtAuthService.getUserIdFromToken(token.replace("Bearer ", "")) : null;
        if (userId == null) {
            return Result.error("请先登录");
        }

        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            return Result.error("无权查看该仲裁详情");
        }

        Map<String, Object> data = disputeService.getDisputeDetail(orderId);
        return Result.success(data);
    }
}
