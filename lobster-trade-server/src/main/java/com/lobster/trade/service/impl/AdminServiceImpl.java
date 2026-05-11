package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.config.AdminContext;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.*;
import com.lobster.trade.model.entity.Admin;
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
import com.lobster.trade.service.EscrowService;
import com.lobster.trade.service.SysNotificationService;
import com.lobster.trade.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final UserMapper userMapper;
    private final TradeOrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final GameCategoryMapper gameCategoryMapper;
    private final EscrowService escrowService;
    private final SysNotificationService sysNotificationService;

    @Override
    public Map<String, Object> login(AdminLoginRequest req) {
        Admin admin = adminMapper.selectByUsername(req.getUsername());
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "管理员账号不存在");
        }
        if (!PasswordEncoder.matches(admin.getPassword(), req.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "密码错误");
        }
        if (admin.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("adminId", admin.getId());
        payload.put("username", admin.getUsername());
        payload.put("role", "ADMIN");
        String token = com.lobster.trade.util.JwtUtil.generateTokenStatic(payload);

        AdminVO adminVO = new AdminVO();
        adminVO.setId(admin.getId());
        adminVO.setUsername(admin.getUsername());
        adminVO.setNickname(admin.getNickname());
        adminVO.setRole(admin.getRole());
        adminVO.setPermissions(admin.getPermissions());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", adminVO);
        return result;
    }

    @Override
    public AdminVO getInfo() {
        Long adminId = AdminContext.get();
        if (adminId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) return null;

        AdminVO vo = new AdminVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setNickname(admin.getNickname());
        vo.setRole(admin.getRole());
        vo.setPermissions(admin.getPermissions());
        return vo;
    }

    // ==================== 用户管理 ====================

    @Override
    public Page<User> listUsers(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            q.eq("1".equals(status), User::getStatus, Integer.parseInt(status));
        }
        q.and(StringUtils.hasText(keyword),
                w -> w.like(StringUtils.hasText(keyword), User::getNickname, keyword)
                   .or().like(StringUtils.hasText(keyword), User::getPhone, keyword)
                   .or().like(StringUtils.hasText(keyword), User::getUsername, keyword)
        );
        q.orderByDesc(User::getCreateTime);
        Page<User> p = new Page<>(page, size);
        return userMapper.selectPage(p, q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "用户不存在");
        }
        user.setStatus(2);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "用户不存在");
        }
        user.setStatus(1);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, Map<String, Object> body) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "用户不存在");
        }
        if (body.get("nickname") != null) user.setNickname((String) body.get("nickname"));
        if (body.get("status") != null) user.setStatus((Integer) body.get("status"));
        if (body.get("userLevel") != null) user.setUserLevel((Integer) body.get("userLevel"));
        if (body.get("balance") != null) user.setBalance(new BigDecimal(body.get("balance").toString()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    // ==================== 订单管理 ====================

    @Override
    public Page<TradeOrder> listOrders(String keyword, String status, String tradeType, int page, int size) {
        // MyBatis-Plus selectPage + wrapper内含or()会导致count查询返回0，改用selectList全量+Java分页
        LambdaQueryWrapper<TradeOrder> q = new LambdaQueryWrapper<>();
        q.eq(StringUtils.hasText(status), TradeOrder::getStatus, status);
        q.eq(StringUtils.hasText(tradeType), TradeOrder::getTradeType, tradeType);
        q.and(StringUtils.hasText(keyword),
                w -> w.like(TradeOrder::getOrderNo, keyword)
                   .or().like(TradeOrder::getProductTitle, keyword)
        );
        q.orderByDesc(TradeOrder::getCreateTime);
        List<TradeOrder> all = orderMapper.selectList(q);
        int total = all.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<TradeOrder> records = fromIndex < total ? all.subList(fromIndex, toIndex) : List.of();
        Page<TradeOrder> p = new Page<>(page, size);
        p.setRecords(records);
        p.setTotal(total);
        p.setPages((total + size - 1) / size);
        return p;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(Long id, Map<String, Object> body) {
        TradeOrder order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        String oldStatus = order.getStatus();

        // 【安全加固】禁止管理员修改订单核心身份字段
        if (body.get("sellerId") != null || body.get("buyerId") != null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "禁止修改订单买卖双方身份");
        }
        // 【安全加固】禁止管理员修改金额相关字段（金额由系统计算）
        if (body.get("orderAmount") != null || body.get("escrowAmount") != null
                || body.get("sellerReceived") != null || body.get("platformFee") != null
                || body.get("commissionRate") != null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "禁止修改订单金额相关字段");
        }

        if (body.get("status") != null) order.setStatus((String) body.get("status"));
        if (body.get("deliveryRemark") != null) order.setDeliveryRemark((String) body.get("deliveryRemark"));
        if (body.get("disputeStatus") != null) order.setDisputeStatus((Integer) body.get("disputeStatus"));
        if (body.get("disputeReason") != null) order.setDisputeReason((String) body.get("disputeReason"));
        if (body.get("disputeResult") != null) order.setDisputeResult((String) body.get("disputeResult"));
        if (body.get("tradeType") != null) order.setTradeType((String) body.get("tradeType"));
        if (body.get("productTitle") != null) order.setProductTitle((String) body.get("productTitle"));
        if (body.get("gameId") != null) order.setGameId(toLong(body.get("gameId")));
        if (body.get("categoryId") != null) order.setCategoryId(toLong(body.get("categoryId")));
        if (body.get("depositSeller") != null) order.setDepositSeller(new BigDecimal(body.get("depositSeller").toString()));
        if (body.get("depositBuyer") != null) order.setDepositBuyer(new BigDecimal(body.get("depositBuyer").toString()));
        if (body.get("escrowStatus") != null) order.setEscrowStatus((Integer) body.get("escrowStatus"));
        if (body.get("paymentStatus") != null) order.setPaymentStatus((Integer) body.get("paymentStatus"));
        if (body.get("buyerCancel") != null) order.setBuyerCancel((Integer) body.get("buyerCancel"));
        if (body.get("refundRequest") != null) order.setRefundRequest((Integer) body.get("refundRequest"));
        if (body.get("refundReason") != null) order.setRefundReason((String) body.get("refundReason"));
        if (body.get("boostRequirement") != null) order.setBoostRequirement((String) body.get("boostRequirement"));
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 【资金处理】状态变更为已完成 → 释放托管资金给卖家
        String newStatus = order.getStatus();
        if (!oldStatus.equals(newStatus) && "completed".equals(newStatus)) {
            escrowService.releaseEscrow(order);
            sysNotificationService.createForUser(order.getSellerId(),
                    "✅ 订单已完成（管理员介入）",
                    "商品【" + order.getProductTitle() + "】订单已完成，款项已到账。订单号：" + order.getOrderNo(),
                    2, "/order/detail/" + order.getId());
            sysNotificationService.createForUser(order.getBuyerId(),
                    "✅ 订单已完成（管理员介入）",
                    "您购买的商品【" + order.getProductTitle() + "】交易已完成，欢迎评价。订单号：" + order.getOrderNo(),
                    2, "/order/detail/" + order.getId());
        }
        // 【资金处理】状态变更为已取消 → 退款给买家
        if (!oldStatus.equals(newStatus) && "cancelled".equals(newStatus)) {
            escrowService.refundEscrow(order);
            sysNotificationService.createForUser(order.getBuyerId(),
                    "❌ 订单已取消（管理员介入）",
                    "您的订单【" + order.getProductTitle() + "】已由管理员取消，款项已退还至钱包。订单号：" + order.getOrderNo(),
                    2, "/order/detail/" + order.getId());
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        return Long.parseLong(val.toString());
    }

    // ==================== 商品管理 ====================

    @Override
    public Page<Product> listProducts(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<Product> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            q.eq("1".equals(status), Product::getStatus, Integer.parseInt(status));
        }
        q.and(StringUtils.hasText(keyword),
                w -> w.like(Product::getTitle, keyword)
                   .or().like(Product::getGameZone, keyword)
        );
        q.orderByDesc(Product::getCreateTime);
        Page<Product> p = new Page<>(page, size);
        return productMapper.selectPage(p, q);
    }

    @Override
    public Page<AdminProductVO> listProductsEnriched(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<Product> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            q.eq(Product::getStatus, Integer.parseInt(status));
        }
        q.and(StringUtils.hasText(keyword),
                w -> w.like(Product::getTitle, keyword)
                   .or().like(Product::getGameZone, keyword)
        );
        q.orderByDesc(Product::getCreateTime);
        Page<Product> p = new Page<>(page, size);
        productMapper.selectPage(p, q);

        List<AdminProductVO> records = new ArrayList<>();
        for (Product prod : p.getRecords()) {
            AdminProductVO vo = new AdminProductVO();
            vo.setId(prod.getId());
            vo.setTitle(prod.getTitle());
            vo.setGameId(prod.getGameId());
            vo.setCategoryId(prod.getCategoryId());
            vo.setProductType(prod.getProductType());
            vo.setDescription(prod.getDescription());
            vo.setImages(prod.getImages());
            vo.setPriceType(prod.getPriceType());
            vo.setPrice(prod.getPrice());
            vo.setUnit(prod.getUnit());
            vo.setGameZone(prod.getGameZone());
            vo.setServer(prod.getServer());
            vo.setPlatform(prod.getPlatform());
            vo.setMinDeposit(prod.getMinDeposit());
            vo.setEstimatedHours(prod.getEstimatedHours());
            vo.setStock(prod.getStock());
            vo.setTotalOrders(prod.getTotalOrders());
            vo.setCompletedOrders(prod.getCompletedOrders());
            vo.setViewCount(prod.getViewCount());
            vo.setFavoriteCount(prod.getFavoriteCount());
            vo.setStatus(prod.getStatus());
            vo.setCreateTime(prod.getCreateTime());
            vo.setSellerId(prod.getSellerId());

            // 解析 images JSON，取封面
            try {
                if (prod.getImages() != null && !prod.getImages().isBlank()) {
                    com.alibaba.fastjson2.JSONArray arr = com.alibaba.fastjson2.JSONArray.parseArray(prod.getImages());
                    if (arr != null && !arr.isEmpty()) {
                        vo.setCoverImage(arr.getString(0));
                    }
                }
            } catch (Exception ignored) {}

            // 游戏名
            GameCategory g = gameCategoryMapper.selectById(prod.getGameId());
            if (g != null) {
                vo.setGameName(g.getGameName());
            }
            // 类目名
            GameCategory c = gameCategoryMapper.selectById(prod.getCategoryId());
            if (c != null) {
                vo.setCategoryName(c.getGameName());
            }
            // 卖家信息
            User u = userMapper.selectById(prod.getSellerId());
            if (u != null) {
                vo.setSellerNickname(u.getNickname());
                vo.setSellerPhone(u.getPhone());
            }
            records.add(vo);
        }

        Page<AdminProductVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void productOff(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        }
        product.setStatus(2);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void productOn(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        }
        product.setStatus(1);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void productBan(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        }
        product.setStatus(3);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, AdminProductUpdateRequest req) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        }
        if (req.getTitle() != null) product.setTitle(req.getTitle());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getImages() != null) product.setImages(req.getImages());
        if (req.getPrice() != null) product.setPrice(req.getPrice());
        if (req.getUnit() != null) product.setUnit(req.getUnit());
        if (req.getPriceType() != null) product.setPriceType(req.getPriceType());
        if (req.getGameZone() != null) product.setGameZone(req.getGameZone());
        if (req.getServer() != null) product.setServer(req.getServer());
        if (req.getPlatform() != null) product.setPlatform(req.getPlatform());
        if (req.getMinDeposit() != null) product.setMinDeposit(req.getMinDeposit());
        if (req.getStock() != null) product.setStock(req.getStock());
        if (req.getEstimatedHours() != null) product.setEstimatedHours(req.getEstimatedHours());
        if (req.getStatus() != null) product.setStatus(req.getStatus());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    // ==================== 纠纷管理 ====================

    @Override
    public Page<TradeOrder> listDisputes(int page, int size) {
        LambdaQueryWrapper<TradeOrder> q = new LambdaQueryWrapper<>();
        q.eq(TradeOrder::getDisputeStatus, 1); // 申诉中
        q.orderByDesc(TradeOrder::getUpdateTime);
        Page<TradeOrder> p = new Page<>(page, size);
        return orderMapper.selectPage(p, q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveDispute(Long orderId, String result) {
        Long adminId = AdminContext.get();
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        order.setDisputeStatus(2); // 已处理
        order.setDisputeResult(result);
        order.setDisputeTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        if (result.contains("退款")) {
            order.setEscrowStatus(3); // 已退款
            order.setStatus("cancelled");
            escrowService.refundEscrow(order);
        } else if (result.contains("打款") || result.contains("放款")) {
            order.setEscrowStatus(2); // 已释放（不是3）
            order.setSellerReceived(order.getEscrowAmount());
            order.setStatus("completed");
            escrowService.releaseEscrow(order);
        }
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDispute(Long orderId, Map<String, Object> body) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (body.get("disputeStatus") != null) order.setDisputeStatus((Integer) body.get("disputeStatus"));
        if (body.get("disputeResult") != null) order.setDisputeResult((String) body.get("disputeResult"));
        if (body.get("status") != null) order.setStatus((String) body.get("status"));
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    // ==================== 游戏管理 ====================

    @Override
    public List<GameCategory> listGames() {
        LambdaQueryWrapper<GameCategory> q = new LambdaQueryWrapper<>();
        q.orderByAsc(GameCategory::getSortOrder);
        return gameCategoryMapper.selectList(q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGame(GameCategory game) {
        game.setCreateTime(LocalDateTime.now());
        game.setUpdateTime(LocalDateTime.now());
        game.setIsDeleted(0);
        if (game.getStatus() == null) game.setStatus(1);
        if (game.getSortOrder() == null) game.setSortOrder(100);
        gameCategoryMapper.insert(game);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGame(Long id, GameCategory game) {
        GameCategory existing = gameCategoryMapper.selectById(id);
        if (existing == null || existing.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "游戏不存在");
        }
        game.setId(id);
        game.setUpdateTime(LocalDateTime.now());
        gameCategoryMapper.updateById(game);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleGameStatus(Long id, int status) {
        GameCategory game = gameCategoryMapper.selectById(id);
        if (game == null || game.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "游戏不存在");
        }
        game.setStatus(status);
        game.setUpdateTime(LocalDateTime.now());
        gameCategoryMapper.updateById(game);
    }

    // ==================== 数据统计 ====================

    @Override
    public Map<String, Object> statsOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 今日新增用户
        LambdaQueryWrapper<User> userQ = new LambdaQueryWrapper<>();
        userQ.ge(User::getCreateTime, startOfDay).le(User::getCreateTime, endOfDay);
        long todayUsers = userMapper.selectCount(userQ);

        // 总用户数
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>());

        // 今日订单
        LambdaQueryWrapper<TradeOrder> orderQ = new LambdaQueryWrapper<>();
        orderQ.ge(TradeOrder::getCreateTime, startOfDay).le(TradeOrder::getCreateTime, endOfDay);
        long todayOrders = orderMapper.selectCount(orderQ);

        // 总订单数
        long totalOrders = orderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>());

        // 今日GMV
        List<TradeOrder> todayOrderList = orderMapper.selectList(orderQ);
        BigDecimal todayGmv = todayOrderList.stream()
                .map(TradeOrder::getOrderAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 商品总数
        long totalProducts = productMapper.selectCount(new LambdaQueryWrapper<Product>());

        // 待处理纠纷
        LambdaQueryWrapper<TradeOrder> disputeQ = new LambdaQueryWrapper<>();
        disputeQ.eq(TradeOrder::getDisputeStatus, 1);
        long pendingDisputes = orderMapper.selectCount(disputeQ);

        // 近7天趋势
        List<Map<String, Object>> orderTrendData = buildOrderTrend(7);

        // 今日用户趋势
        List<Map<String, Object>> trendUsers = new ArrayList<>();
        LocalDate today2 = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today2.minusDays(i);
            LocalDateTime s = d.atStartOfDay();
            LocalDateTime e = d.atTime(LocalTime.MAX);
            LambdaQueryWrapper<User> uq = new LambdaQueryWrapper<>();
            uq.ge(User::getCreateTime, s).le(User::getCreateTime, e);
            long uc = userMapper.selectCount(uq);
            Map<String, Object> uitem = new HashMap<>();
            uitem.put("date", d.toString());
            uitem.put("count", uc);
            trendUsers.add(uitem);
        }

        // 总GMV
        BigDecimal totalGmv = orderMapper.selectList(new LambdaQueryWrapper<TradeOrder>())
                .stream().map(TradeOrder::getOrderAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 最近订单
        LambdaQueryWrapper<TradeOrder> recentQ = new LambdaQueryWrapper<>();
        recentQ.orderByDesc(TradeOrder::getCreateTime).last("LIMIT 10");
        List<TradeOrder> recentOrderList = orderMapper.selectList(recentQ);

        Map<String, Object> data = new HashMap<>();
        data.put("todayUsers", todayUsers);
        data.put("totalUsers", totalUsers);
        data.put("todayOrders", todayOrders);
        data.put("totalOrders", totalOrders);
        data.put("todayGmv", todayGmv);
        data.put("totalProducts", totalProducts);
        data.put("pendingDisputes", pendingDisputes);
        data.put("todayGmvFormatted", formatMoney(todayGmv));
        data.put("totalGmv", totalGmv);
        data.put("todayUsersTrend", trendUsers);
        data.put("orderTrend", orderTrendData);
        data.put("recentOrders", recentOrderList.stream().limit(10).map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("productTitle", o.getProductTitle());
            m.put("buyerId", o.getBuyerId());
            m.put("sellerId", o.getSellerId());
            m.put("orderAmount", o.getOrderAmount());
            m.put("status", o.getStatus());
            m.put("tradeType", o.getTradeType());
            m.put("createTime", o.getCreateTime() != null ? o.getCreateTime().toString().substring(0, 19) : "");
            return m;
        }).collect(Collectors.toList()));

        return data;
    }

    private String formatMoney(BigDecimal v) {
        if (v == null || v.compareTo(BigDecimal.ZERO) == 0) return "0.00";
        if (v.compareTo(new BigDecimal(10000)) >= 0) {
            return String.format("%.1f万", v.divide(new BigDecimal(10000)).doubleValue());
        }
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }

    private List<Map<String, Object>> buildOrderTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        String[] labels = {"周日","周一","周二","周三","周四","周五","周六"};
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.atTime(LocalTime.MAX);
            LambdaQueryWrapper<TradeOrder> q = new LambdaQueryWrapper<>();
            q.ge(TradeOrder::getCreateTime, start).le(TradeOrder::getCreateTime, end);
            List<TradeOrder> list = orderMapper.selectList(q);
            long count = list.size();
            BigDecimal gmv = list.stream().map(TradeOrder::getOrderAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> item = new HashMap<>();
            item.put("date", d.toString());
            item.put("label", labels[d.getDayOfWeek().getValue() % 7]);
            item.put("count", count);
            item.put("gmv", gmv);
            trend.add(item);
        }
        return trend;
    }

    // ==================== 权限相关 ====================

    private Admin getCurrentAdmin() {
        Long adminId = AdminContext.get();
        if (adminId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) throw new BusinessException(ErrorCode.UNAUTHORIZED, "管理员不存在");
        return admin;
    }

    @Override
    public boolean hasPermission(String permission) {
        Admin admin = getCurrentAdmin();
        if ("*".equals(admin.getPermissions())) return true;
        if (admin.getPermissions() == null) return false;
        for (String p : admin.getPermissions().split(",")) {
            if (p.trim().equals(permission)) return true;
        }
        return false;
    }

    @Override
    public boolean hasAnyPermission(String... perms) {
        for (String p : perms) {
            if (hasPermission(p)) return true;
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(String... perms) {
        for (String p : perms) {
            if (!hasPermission(p)) return false;
        }
        return true;
    }

    @Override
    public void grantPermissions(Long adminId, String permissions) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) throw new BusinessException(ErrorCode.NOT_FOUND, "管理员不存在");
        admin.setPermissions(permissions);
        adminMapper.updateById(admin);
    }

    @Override
    public void revokePermissions(Long adminId, String permission) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) throw new BusinessException(ErrorCode.NOT_FOUND, "管理员不存在");
        if (admin.getPermissions() == null || "*".equals(admin.getPermissions())) return;
        String[] arr = admin.getPermissions().split(",");
        String remaining = java.util.Arrays.stream(arr)
                .filter(p -> !p.trim().equals(permission))
                .collect(Collectors.joining(","));
        admin.setPermissions(remaining);
        adminMapper.updateById(admin);
    }

    // ==================== 管理员CRUD ====================

    @Override
    public List<Admin> listAdmins(String keyword, int page, int size) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有管理员管理权限");
        }
        LambdaQueryWrapper<Admin> q = new LambdaQueryWrapper<>();
        q.ne(Admin::getRole, "SUPER_ADMIN"); // 不显示超级管理员
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(Admin::getUsername, keyword).or().like(Admin::getNickname, keyword));
        }
        q.orderByDesc(Admin::getCreateTime);
        Page<Admin> p = new Page<>(page, size);
        adminMapper.selectPage(p, q);
        return p.getRecords();
    }

    @Override
    public Admin getAdminById(Long id) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有管理员管理权限");
        }
        Admin admin = adminMapper.selectById(id);
        if (admin != null) admin.setPassword(null); // 不返回密码
        return admin;
    }

    @Override
    public void createAdmin(Admin admin) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有管理员管理权限");
        }
        if (adminMapper.selectByUsername(admin.getUsername()) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }
        admin.setId(null);
        admin.setPassword(PasswordEncoder.encode(admin.getPassword()));
        if (admin.getPermissions() == null) admin.setPermissions("");
        if (admin.getRole() == null) admin.setRole("ADMIN");
        if (admin.getStatus() == null) admin.setStatus(1);
        adminMapper.insert(admin);
    }

    @Override
    public void updateAdmin(Long id, Admin admin) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有管理员管理权限");
        }
        Admin exist = adminMapper.selectById(id);
        if (exist == null) throw new BusinessException(ErrorCode.NOT_FOUND, "管理员不存在");
        if ("SUPER_ADMIN".equals(exist.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能修改超级管理员信息");
        }
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            exist.setPassword(PasswordEncoder.encode(admin.getPassword()));
        }
        if (admin.getNickname() != null) exist.setNickname(admin.getNickname());
        if (admin.getRole() != null) exist.setRole(admin.getRole());
        if (admin.getPermissions() != null) exist.setPermissions(admin.getPermissions());
        if (admin.getStatus() != null) exist.setStatus(admin.getStatus());
        adminMapper.updateById(exist);
    }

    @Override
    public void deleteAdmin(Long id) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有管理员管理权限");
        }
        Admin exist = adminMapper.selectById(id);
        if (exist == null) throw new BusinessException(ErrorCode.NOT_FOUND, "管理员不存在");
        if ("SUPER_ADMIN".equals(exist.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能删除超级管理员");
        }
        adminMapper.deleteById(id);
    }

    @Override
    public void updateAdminStatus(Long id, int status) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有管理员管理权限");
        }
        Admin exist = adminMapper.selectById(id);
        if (exist == null) throw new BusinessException(ErrorCode.NOT_FOUND, "管理员不存在");
        if ("SUPER_ADMIN".equals(exist.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能禁用超级管理员");
        }
        exist.setStatus(status);
        adminMapper.updateById(exist);
    }

    // ==================== 角色CRUD ====================

    @Override
    public List<AdminRole> listRoles() {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getStatus, 1));
    }

    @Override
    public AdminRole getRoleById(Long id) {
        return adminRoleMapper.selectById(id);
    }

    @Override
    public void createRole(AdminRole role) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限");
        }
        adminRoleMapper.insert(role);
    }

    @Override
    public void updateRole(Long id, AdminRole role) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限");
        }
        AdminRole exist = adminRoleMapper.selectById(id);
        if (exist == null) throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        if (role.getRoleName() != null) exist.setRoleName(role.getRoleName());
        if (role.getPermissions() != null) exist.setPermissions(role.getPermissions());
        if (role.getDescription() != null) exist.setDescription(role.getDescription());
        if (role.getStatus() != null) exist.setStatus(role.getStatus());
        adminRoleMapper.updateById(exist);
    }

    @Override
    public void deleteRole(Long id) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限");
        }
        adminRoleMapper.deleteById(id);
    }

    @Override
    public void updateRoleStatus(Long id, int status) {
        AdminRole exist = adminRoleMapper.selectById(id);
        if (exist == null) throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        exist.setStatus(status);
        adminRoleMapper.updateById(exist);
    }

    @Override
    public void assignRole(Long adminId, String roleCode) {
        if (!hasPermission(AdminPermission.ADMIN_MANAGE) && !hasPermission("*")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限");
        }
        AdminRole role = adminRoleMapper.selectByRoleCode(roleCode);
        if (role == null) throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) throw new BusinessException(ErrorCode.NOT_FOUND, "管理员不存在");
        admin.setRole(roleCode);
        admin.setPermissions(role.getPermissions());
        adminMapper.updateById(admin);
    }
}