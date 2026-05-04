package com.lobster.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.annotation.Audit;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.mapper.AdminAuditLogMapper;
import com.lobster.trade.model.entity.Admin;
import com.lobster.trade.model.entity.AdminAuditLog;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.entity.AdminRole;
import com.lobster.trade.model.entity.GameCategory;
import com.lobster.trade.model.entity.Product;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.request.AdminLoginRequest;
import com.lobster.trade.model.request.AdminProductUpdateRequest;
import com.lobster.trade.model.response.AdminProductVO;
import com.lobster.trade.model.response.AdminVO;
import com.lobster.trade.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminAuditLogMapper adminAuditLogMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody AdminLoginRequest req) {
        return Result.success(adminService.login(req));
    }

    @GetMapping("/info")
    public Result<AdminVO> getInfo() {
        return Result.success(adminService.getInfo());
    }

    @GetMapping("/stats/overview")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Map<String, Object>> stats() {
        return Result.success(adminService.statsOverview());
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    @RequirePermission(AdminPermission.USER_VIEW)
    public Result<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<User> p = adminService.listUsers(keyword, status, page, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("records", p.getRecords());
        result.put("total", p.getTotal());
        return Result.success(result);
    }

    @Audit(value = "封禁用户", targetType = "User")
    @PostMapping("/user/{id}/ban")
    @RequirePermission(AdminPermission.USER_BAN)
    public Result<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return Result.success(null);
    }

    @Audit(value = "解封用户", targetType = "User")
    @PostMapping("/user/{id}/unban")
    @RequirePermission(AdminPermission.USER_BAN)
    public Result<Void> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return Result.success(null);
    }

    @Audit(value = "修改用户", targetType = "User")
    @PutMapping("/user/{id}")
    @RequirePermission(AdminPermission.USER_EDIT)
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminService.updateUser(id, body);
        return Result.success(null);
    }

    // ==================== 订单管理 ====================

    @GetMapping("/orders")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<Map<String, Object>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tradeType,
            @RequestParam(required = false) String keyword) {
        Page<TradeOrder> p = adminService.listOrders(keyword, status, tradeType, page, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("records", p.getRecords());
        result.put("total", p.getTotal());
        return Result.success(result);
    }

    @GetMapping("/orders/recent")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<List<TradeOrder>> recentOrders() {
        Page<TradeOrder> p = adminService.listOrders(null, null, null, 1, 10);
        return Result.success(p.getRecords());
    }

    @Audit(value = "修改订单", targetType = "Order")
    @PutMapping("/order/{id}")
    @RequirePermission(AdminPermission.ORDER_EDIT)
    public Result<Void> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminService.updateOrder(id, body);
        return Result.success(null);
    }

    // ==================== 商品管理 ====================

    @GetMapping("/products")
    @RequirePermission(AdminPermission.PRODUCT_VIEW)
    public Result<Map<String, Object>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Page<AdminProductVO> p = adminService.listProductsEnriched(keyword, status, page, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("records", p.getRecords());
        result.put("total", p.getTotal());
        return Result.success(result);
    }

    @Audit(value = "下架商品", targetType = "Product")
    @PostMapping("/product/{id}/off")
    @RequirePermission(AdminPermission.PRODUCT_EDIT)
    public Result<Void> productOff(@PathVariable Long id) {
        adminService.productOff(id);
        return Result.success(null);
    }

    @Audit(value = "上架商品", targetType = "Product")
    @PostMapping("/product/{id}/on")
    @RequirePermission(AdminPermission.PRODUCT_EDIT)
    public Result<Void> productOn(@PathVariable Long id) {
        adminService.productOn(id);
        return Result.success(null);
    }

    @Audit(value = "封禁商品", targetType = "Product")
    @PostMapping("/product/{id}/ban")
    @RequirePermission(AdminPermission.PRODUCT_AUDIT)
    public Result<Void> productBan(@PathVariable Long id) {
        adminService.productBan(id);
        return Result.success(null);
    }

    @Audit(value = "修改商品", targetType = "Product")
    @PutMapping("/product/{id}")
    @RequirePermission(AdminPermission.PRODUCT_EDIT)
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody AdminProductUpdateRequest req) {
        adminService.updateProduct(id, req);
        return Result.success(null);
    }

    // ==================== 纠纷管理 ====================

    @GetMapping("/disputes")
    @RequirePermission(AdminPermission.DISPUTE_VIEW)
    public Result<Map<String, Object>> listDisputes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<TradeOrder> p = adminService.listDisputes(page, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("records", p.getRecords());
        result.put("total", p.getTotal());
        return Result.success(result);
    }

    @GetMapping("/disputes/stats")
    @RequirePermission(AdminPermission.DISPUTE_VIEW)
    public Result<Map<String, Integer>> disputeStats() {
        Page<TradeOrder> p = adminService.listDisputes(1, 1);
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", (int) p.getTotal());
        return Result.success(stats);
    }

    @Audit(value = "处理仲裁", targetType = "Dispute")
    @PostMapping("/dispute/resolve/{orderId}")
    @RequirePermission(AdminPermission.DISPUTE_HANDLE)
    public Result<Void> resolveDispute(@PathVariable Long orderId, @RequestBody Map<String, String> body) {
        adminService.resolveDispute(orderId, body.get("result"));
        return Result.success(null);
    }

    @Audit(value = "编辑仲裁", targetType = "Dispute")
    @PutMapping("/dispute/{orderId}")
    @RequirePermission(AdminPermission.DISPUTE_HANDLE)
    public Result<Void> updateDispute(@PathVariable Long orderId, @RequestBody Map<String, Object> body) {
        adminService.updateDispute(orderId, body);
        return Result.success(null);
    }

    // ==================== 游戏管理 ====================

    @GetMapping("/games")
    @RequirePermission(AdminPermission.GAME_VIEW)
    public Result<List<GameCategory>> listGames() {
        return Result.success(adminService.listGames());
    }

    @Audit(value = "新增游戏", targetType = "Game")
    @PostMapping("/game")
    @RequirePermission(AdminPermission.GAME_EDIT)
    public Result<Void> createGame(@RequestBody GameCategory game) {
        adminService.createGame(game);
        return Result.success(null);
    }

    @Audit(value = "修改游戏", targetType = "Game")
    @PutMapping("/game/{id}")
    @RequirePermission(AdminPermission.GAME_EDIT)
    public Result<Void> updateGame(@PathVariable Long id, @RequestBody GameCategory game) {
        adminService.updateGame(id, game);
        return Result.success(null);
    }

    @Audit(value = "修改游戏状态", targetType = "Game")
    @PostMapping("/game/{id}/status")
    @RequirePermission(AdminPermission.GAME_EDIT)
    public Result<Void> toggleGameStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.toggleGameStatus(id, body.get("status"));
        return Result.success(null);
    }

    // ==================== 管理员管理（仅超级管理员） ====================

    @GetMapping("/admins")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Map<String, Object>> listAdmins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        List<Admin> list = adminService.listAdmins(keyword, page, pageSize);
        // 脱敏密码
        list.forEach(a -> a.setPassword(null));
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", list.size());
        return Result.success(result);
    }

    @GetMapping("/admin/{id}")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Admin> getAdmin(@PathVariable Long id) {
        return Result.success(adminService.getAdminById(id));
    }

    @Audit(value = "新增管理员", targetType = "Admin")
    @PostMapping("/admin")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> createAdmin(@RequestBody Admin admin) {
        adminService.createAdmin(admin);
        return Result.success(null);
    }

    @Audit(value = "修改管理员", targetType = "Admin")
    @PutMapping("/admin/{id}")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        adminService.updateAdmin(id, admin);
        return Result.success(null);
    }

    @Audit(value = "删除管理员", targetType = "Admin")
    @DeleteMapping("/admin/{id}")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return Result.success(null);
    }

    @Audit(value = "启用/禁用管理员", targetType = "Admin")
    @PostMapping("/admin/{id}/status")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> updateAdminStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateAdminStatus(id, body.get("status"));
        return Result.success(null);
    }

    @Audit(value = "修改管理员权限", targetType = "Admin")
    @PostMapping("/admin/{id}/permissions")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> grantPermissions(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.grantPermissions(id, body.get("permissions"));
        return Result.success(null);
    }

    @PostMapping("/admin/{id}/assign-role")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> assignRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.assignRole(id, body.get("roleCode"));
        return Result.success(null);
    }

    // ==================== 角色管理 ====================

    @GetMapping("/roles")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<List<AdminRole>> listRoles() {
        return Result.success(adminService.listRoles());
    }

    @PostMapping("/role")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> createRole(@RequestBody AdminRole role) {
        adminService.createRole(role);
        return Result.success(null);
    }

    @PutMapping("/role/{id}")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody AdminRole role) {
        adminService.updateRole(id, role);
        return Result.success(null);
    }

    @DeleteMapping("/role/{id}")
    @RequirePermission(AdminPermission.ADMIN_MANAGE)
    public Result<Void> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id);
        return Result.success(null);
    }

    // ==================== 认证管理 ====================

    @GetMapping("/certifications")
    @RequirePermission(AdminPermission.CERT_VIEW)
    public Result<Map<String, Object>> listCertifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(adminService.listCertifications(status, page, pageSize));
    }

    @Audit(value = "审核实名认证-通过", targetType = "Certification")
    @PostMapping("/certification/{id}/approve")
    @RequirePermission(AdminPermission.CERT_HANDLE)
    public Result<Void> approveCertification(@PathVariable Long id) {
        adminService.approveCertification(id);
        return Result.success(null);
    }

    @Audit(value = "审核实名认证-拒绝", targetType = "Certification")
    @PostMapping("/certification/{id}/reject")
    @RequirePermission(AdminPermission.CERT_HANDLE)
    public Result<Void> rejectCertification(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.rejectCertification(id, body.get("reason"));
        return Result.success(null);
    }

    // ==================== 优惠券管理 ====================

    @GetMapping("/coupons")
    @RequirePermission(AdminPermission.COUPON_VIEW)
    public Result<Map<String, Object>> listCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(adminService.listCoupons(status, page, pageSize));
    }

    @Audit(value = "新增优惠券", targetType = "Coupon")
    @PostMapping("/coupon")
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> createCoupon(@RequestBody Map<String, Object> body) {
        adminService.createCoupon(body);
        return Result.success(null);
    }

    @Audit(value = "修改优惠券", targetType = "Coupon")
    @PutMapping("/coupon/{id}")
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> updateCoupon(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminService.updateCoupon(id, body);
        return Result.success(null);
    }

    @Audit(value = "删除优惠券", targetType = "Coupon")
    @DeleteMapping("/coupon/{id}")
    @RequirePermission(AdminPermission.COUPON_EDIT)
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        adminService.deleteCoupon(id);
        return Result.success(null);
    }

    // ==================== 热词管理 ====================

    @GetMapping("/hot-search")
    @RequirePermission(AdminPermission.HOTSEARCH_VIEW)
    public Result<Map<String, Object>> listHotSearch(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(adminService.listHotSearch(page, pageSize));
    }

    @Audit(value = "新增热词", targetType = "HotSearch")
    @PostMapping("/hot-search")
    @RequirePermission(AdminPermission.HOTSEARCH_EDIT)
    public Result<Void> createHotSearch(@RequestBody Map<String, Object> body) {
        adminService.createHotSearch(body);
        return Result.success(null);
    }

    @Audit(value = "修改热词", targetType = "HotSearch")
    @PutMapping("/hot-search/{id}")
    @RequirePermission(AdminPermission.HOTSEARCH_EDIT)
    public Result<Void> updateHotSearch(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminService.updateHotSearch(id, body);
        return Result.success(null);
    }

    @Audit(value = "删除热词", targetType = "HotSearch")
    @DeleteMapping("/hot-search/{id}")
    @RequirePermission(AdminPermission.HOTSEARCH_EDIT)
    public Result<Void> deleteHotSearch(@PathVariable Long id) {
        adminService.deleteHotSearch(id);
        return Result.success(null);
    }

    // ==================== 审计日志 ====================

    /**
     * 审计日志查询
     */
    @GetMapping("/audit-logs")
    @RequirePermission(AdminPermission.AUDIT_VIEW)
    public Result<Page<AdminAuditLog>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long adminId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        LambdaQueryWrapper<AdminAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (action != null && !action.isEmpty()) wrapper.eq(AdminAuditLog::getAction, action);
        if (targetType != null && !targetType.isEmpty()) wrapper.eq(AdminAuditLog::getTargetType, targetType);
        if (adminId != null) wrapper.eq(AdminAuditLog::getAdminId, adminId);
        wrapper.orderByDesc(AdminAuditLog::getCreateTime);
        Page<AdminAuditLog> pageParam = new Page<>(page, size);
        return Result.success(adminAuditLogMapper.selectPage(pageParam, wrapper));
    }

    // ==================== 权限信息 ====================

    @GetMapping("/permissions")
    public Result<Map<String, String>> listAllPermissions() {
        // 返回所有权限的说明
        Map<String, String> perms = new HashMap<>();
        perms.put(AdminPermission.USER_VIEW, "查看用户");
        perms.put(AdminPermission.USER_EDIT, "编辑用户");
        perms.put(AdminPermission.USER_BAN, "禁用/解封用户");
        perms.put(AdminPermission.ORDER_VIEW, "查看订单");
        perms.put(AdminPermission.ORDER_EDIT, "编辑订单");
        perms.put(AdminPermission.PRODUCT_VIEW, "查看商品");
        perms.put(AdminPermission.PRODUCT_EDIT, "编辑商品");
        perms.put(AdminPermission.PRODUCT_AUDIT, "审核/封禁商品");
        perms.put(AdminPermission.DISPUTE_VIEW, "查看纠纷");
        perms.put(AdminPermission.DISPUTE_HANDLE, "处理纠纷");
        perms.put(AdminPermission.GAME_VIEW, "查看游戏");
        perms.put(AdminPermission.GAME_EDIT, "编辑游戏");
        perms.put(AdminPermission.ANNOUNCEMENT_VIEW, "查看公告");
        perms.put(AdminPermission.ANNOUNCEMENT_EDIT, "编辑公告");
        perms.put(AdminPermission.COUPON_VIEW, "查看优惠券");
        perms.put(AdminPermission.COUPON_EDIT, "编辑优惠券");
        perms.put(AdminPermission.CS_VIEW, "查看客服会话");
        perms.put(AdminPermission.CS_HANDLE, "处理客服会话");
        perms.put(AdminPermission.CERT_VIEW, "查看实名认证");
        perms.put(AdminPermission.CERT_HANDLE, "审核实名认证");
        perms.put(AdminPermission.NOTIFICATION_VIEW, "查看系统通知");
        perms.put(AdminPermission.NOTIFICATION_EDIT, "发送系统通知");
        perms.put(AdminPermission.TICKET_VIEW, "查看工单");
        perms.put(AdminPermission.TICKET_HANDLE, "处理工单");
        perms.put(AdminPermission.REVIEW_VIEW, "查看评价");
        perms.put(AdminPermission.REVIEW_EDIT, "编辑/隐藏评价");
        perms.put(AdminPermission.HOTSEARCH_VIEW, "查看热搜词");
        perms.put(AdminPermission.HOTSEARCH_EDIT, "编辑热搜词");
        perms.put(AdminPermission.RECSLOT_VIEW, "查看推荐位");
        perms.put(AdminPermission.RECSLOT_EDIT, "编辑推荐位");
        perms.put(AdminPermission.AUDIT_VIEW, "查看审计日志");
        perms.put(AdminPermission.ADMIN_MANAGE, "管理员管理（含权限分配）");
        return Result.success(perms);
    }
}