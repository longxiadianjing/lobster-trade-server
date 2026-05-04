package com.lobster.trade.controller.admin;

import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.model.response.OrderDetailVO;
import com.lobster.trade.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 管理员手动干预订单状态
     * POST /api/admin/order/force-update
     */
    @PostMapping("/force-update")
    @RequirePermission(AdminPermission.ORDER_EDIT)
    public ApiResponse<Void> forceUpdate(@RequestBody AdminOrderUpdateRequest req) {
        orderService.adminUpdateStatus(req.getOrderId(), req.getStatus(), req.getReason());
        return ApiResponse.success("订单状态已更新");
    }

    /**
     * 管理员获取订单详情（不受用户权限限制）
     * GET /api/admin/order/{id}
     */
    @GetMapping("/{id}")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public ApiResponse<OrderDetailVO> getDetail(@PathVariable Long id) {
        return ApiResponse.success(orderService.getAdminDetail(id));
    }

    @Data
    public static class AdminOrderUpdateRequest {
        private Long orderId;
        private String status;     // paid / in_progress / submitted / confirmed / completed / cancelled / disputed
        private String reason;     // 干预原因
    }
}
