package com.lobster.trade.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.model.entity.Admin;
import com.lobster.trade.model.entity.AdminRole;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.model.request.AdminLoginRequest;
import java.util.List;
import java.util.Map;

public interface AdminService {

    Map<String, Object> login(AdminLoginRequest req);

    com.lobster.trade.model.response.AdminVO getInfo();

    // 用户管理
    Page<com.lobster.trade.model.entity.User> listUsers(String keyword, String status, int page, int size);
    void banUser(Long userId);
    void unbanUser(Long userId);
    void updateUser(Long id, Map<String, Object> body);
    String resetUserPassword(Long id);

    // 订单管理
    Page<com.lobster.trade.model.entity.TradeOrder> listOrders(String keyword, String status, String tradeType, int page, int size);
    void updateOrder(Long id, Map<String, Object> body);

    // 商品管理
    Page<com.lobster.trade.model.entity.Product> listProducts(String keyword, String status, int page, int size);
    Page<com.lobster.trade.model.response.AdminProductVO> listProductsEnriched(String keyword, String status, int page, int size);
    void updateProduct(Long id, com.lobster.trade.model.request.AdminProductUpdateRequest req);
    void productOff(Long productId);
    void productOn(Long productId);
    void productBan(Long productId);

    // 纠纷管理
    Page<com.lobster.trade.model.entity.TradeOrder> listDisputes(int page, int size);
    void resolveDispute(Long orderId, String result);
    void updateDispute(Long orderId, Map<String, Object> body);

    // 游戏管理
    List<com.lobster.trade.model.entity.GameCategory> listGames();
    void createGame(com.lobster.trade.model.entity.GameCategory game);
    void updateGame(Long id, com.lobster.trade.model.entity.GameCategory game);
    void toggleGameStatus(Long id, int status);

    // 数据统计
    Map<String, Object> statsOverview();

    // ========== 管理员和角色管理 ==========

    // 管理员CRUD
    List<Admin> listAdmins(String keyword, int page, int size);
    Admin getAdminById(Long id);
    void createAdmin(Admin admin);
    void updateAdmin(Long id, Admin admin);
    void deleteAdmin(Long id);
    void updateAdminStatus(Long id, int status);

    // 权限相关
    boolean hasPermission(String permission);
    boolean hasAnyPermission(String... perms);
    boolean hasAllPermissions(String... perms);
    void grantPermissions(Long adminId, String permissions);
    void revokePermissions(Long adminId, String permission);

    // 角色CRUD
    List<AdminRole> listRoles();
    AdminRole getRoleById(Long id);
    void createRole(AdminRole role);
    void updateRole(Long id, AdminRole role);
    void deleteRole(Long id);
    void updateRoleStatus(Long id, int status);

    // 分配角色
    void assignRole(Long adminId, String roleCode);
}