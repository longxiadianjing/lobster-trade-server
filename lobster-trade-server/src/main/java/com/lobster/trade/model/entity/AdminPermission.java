package com.lobster.trade.model.entity;

/**
 * 后台权限常量
 */
public final class AdminPermission {

    public static final String ALL = "*"; // 拥有全部权限

    // 用户管理
    public static final String USER_VIEW = "USER_VIEW";         // 查看用户
    public static final String USER_EDIT = "USER_EDIT";           // 编辑/修改用户
    public static final String USER_BAN = "USER_BAN";            // 禁用/解封用户

    // 订单管理
    public static final String ORDER_VIEW = "ORDER_VIEW";        // 查看订单
    public static final String ORDER_EDIT = "ORDER_EDIT";        // 修改订单状态

    // 商品管理
    public static final String PRODUCT_VIEW = "PRODUCT_VIEW";    // 查看商品
    public static final String PRODUCT_EDIT = "PRODUCT_EDIT";    // 修改/编辑商品
    public static final String PRODUCT_AUDIT = "PRODUCT_AUDIT";  // 审核/封禁商品

    // 纠纷管理
    public static final String DISPUTE_VIEW = "DISPUTE_VIEW";   // 查看纠纷
    public static final String DISPUTE_HANDLE = "DISPUTE_HANDLE"; // 处理纠纷

    // 游戏管理
    public static final String GAME_VIEW = "GAME_VIEW";         // 查看游戏
    public static final String GAME_EDIT = "GAME_EDIT";          // 编辑/新增游戏

    // 公告管理
    public static final String ANNOUNCEMENT_VIEW = "ANNOUNCEMENT_VIEW"; // 查看公告
    public static final String ANNOUNCEMENT_EDIT = "ANNOUNCEMENT_EDIT";   // 发布/编辑公告

    // 优惠券
    public static final String COUPON_VIEW = "COUPON_VIEW";       // 查看优惠券
    public static final String COUPON_EDIT = "COUPON_EDIT";       // 编辑优惠券

    // 客服
    public static final String CS_VIEW = "CS_VIEW";              // 查看客服会话
    public static final String CS_HANDLE = "CS_HANDLE";          // 处理客服会话

    // 实名认证
    public static final String CERT_VIEW = "CERT_VIEW";         // 查看认证申请
    public static final String CERT_HANDLE = "CERT_HANDLE";      // 审核认证

    // 系统通知
    public static final String NOTIFICATION_VIEW = "NOTIFICATION_VIEW"; // 查看通知
    public static final String NOTIFICATION_EDIT = "NOTIFICATION_EDIT";  // 发送通知

    // 工单
    public static final String TICKET_VIEW = "TICKET_VIEW";      // 查看工单
    public static final String TICKET_HANDLE = "TICKET_HANDLE";    // 处理工单

    // 评价管理
    public static final String REVIEW_VIEW = "REVIEW_VIEW";     // 查看评价
    public static final String REVIEW_EDIT = "REVIEW_EDIT";      // 编辑/隐藏评价

    // 热搜词
    public static final String HOTSEARCH_VIEW = "HOTSEARCH_VIEW";   // 查看热搜词
    public static final String HOTSEARCH_EDIT = "HOTSEARCH_EDIT";    // 编辑热搜词

    // 推荐位
    public static final String RECSLOT_VIEW = "RECSLOT_VIEW";       // 查看推荐位
    public static final String RECSLOT_EDIT = "RECSLOT_EDIT";         // 编辑推荐位

    // 仪表盘/统计
    public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";  // 查看仪表盘统计

    // 审计日志
    public static final String AUDIT_VIEW = "AUDIT_VIEW";           // 查看审计日志

    // 管理员管理
    public static final String ADMIN_MANAGE = "ADMIN_MANAGE";       // 管理其他管理员（包括权限分配）

    private AdminPermission() {}
}