package com.lobster.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.mapper.WalletTransactionMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.entity.WalletTransaction;
import com.lobster.trade.model.entity.AdminPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 财务统计接口
 * 路径：/api/admin/finance/*
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final TradeOrderMapper orderMapper;
    private final WalletTransactionMapper walletTransMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 财务概览统计
     * GET /api/admin/finance/stats-overview
     */
    @GetMapping("/stats-overview")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<Map<String, Object>> financeStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.toLocalDate().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = now.toLocalDate().minusDays(30).atStartOfDay();

        // GMV
        BigDecimal todayGmv = sumOrders(orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")
                        .ge(TradeOrder::getCreateTime, todayStart)));

        BigDecimal weekGmv = sumOrders(orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")
                        .ge(TradeOrder::getCreateTime, weekStart)));

        BigDecimal monthGmv = sumOrders(orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")
                        .ge(TradeOrder::getCreateTime, monthStart)));

        BigDecimal totalGmv = sumOrders(orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")));

        // 充值 type=1, 提现 type=2
        BigDecimal todayRecharge = sumWallet(walletTransMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, 1).eq(WalletTransaction::getStatus, 1)
                        .ge(WalletTransaction::getCreateTime, todayStart)));
        BigDecimal monthRecharge = sumWallet(walletTransMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, 1).eq(WalletTransaction::getStatus, 1)
                        .ge(WalletTransaction::getCreateTime, monthStart)));
        BigDecimal todayWithdraw = sumWallet(walletTransMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, 2).eq(WalletTransaction::getStatus, 1)
                        .ge(WalletTransaction::getCreateTime, todayStart)));
        BigDecimal monthWithdraw = sumWallet(walletTransMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, 2).eq(WalletTransaction::getStatus, 1)
                        .ge(WalletTransaction::getCreateTime, monthStart)));

        // 平台服务费 = 已完成订单的托管金额 - 卖家实收
        BigDecimal platformIncome = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .eq(TradeOrder::getStatus, "completed")
                        .ge(TradeOrder::getCreateTime, monthStart))
                .stream()
                .map(o -> {
                    if (o.getEscrowAmount() == null || o.getSellerReceived() == null) return BigDecimal.ZERO;
                    return o.getEscrowAmount().subtract(o.getSellerReceived());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long todayOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>().ge(TradeOrder::getCreateTime, todayStart));
        long monthOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>().ge(TradeOrder::getCreateTime, monthStart));
        long totalUsers = userMapper.selectCount(null);

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayGmv", todayGmv);
        stats.put("weekGmv", weekGmv);
        stats.put("monthGmv", monthGmv);
        stats.put("totalGmv", totalGmv);
        stats.put("todayRecharge", todayRecharge);
        stats.put("monthRecharge", monthRecharge);
        stats.put("todayWithdraw", todayWithdraw);
        stats.put("monthWithdraw", monthWithdraw);
        stats.put("platformIncome", platformIncome);
        stats.put("todayOrders", todayOrders);
        stats.put("monthOrders", monthOrders);
        stats.put("totalUsers", totalUsers);
        return Result.success(stats);
    }

    /**
     * 近N天 GMV 趋势
     * GET /api/admin/finance/gmv-trend?days=30
     */
    @GetMapping("/gmv-trend")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<List<Map<String, Object>>> gmvTrend(@RequestParam(defaultValue = "30") int days) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().minusDays(days).atStartOfDay();
        Map<LocalDate, BigDecimal> byDate = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")
                        .ge(TradeOrder::getCreateTime, start))
                .stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO,
                                o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO,
                                BigDecimal::add)));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.format(DATE));
            point.put("gmv", byDate.getOrDefault(d, BigDecimal.ZERO));
            result.add(point);
        }
        return Result.success(result);
    }

    /**
     * 近N天每日订单量趋势
     * GET /api/admin/finance/order-trend?days=30
     */
    @GetMapping("/order-trend")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<List<Map<String, Object>>> orderTrend(@RequestParam(defaultValue = "30") int days) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().minusDays(days).atStartOfDay();
        Map<LocalDate, Long> byDate = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .ge(TradeOrder::getCreateTime, start))
                .stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate(),
                        Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.format(DATE));
            point.put("count", byDate.getOrDefault(d, 0L));
            result.add(point);
        }
        return Result.success(result);
    }

    /**
     * 交易类型分布（近30天）
     * GET /api/admin/finance/trade-type-dist
     */
    @GetMapping("/trade-type-dist")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<List<Map<String, Object>>> tradeTypeDist() {
        LocalDateTime monthStart = LocalDateTime.now().toLocalDate().minusDays(30).atStartOfDay();
        Map<String, Long> byType = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .ge(TradeOrder::getCreateTime, monthStart))
                .stream()
                .filter(o -> o.getTradeType() != null)
                .collect(Collectors.groupingBy(TradeOrder::getTradeType, Collectors.counting()));

        return Result.success(byType.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("type", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                }).collect(Collectors.toList()));
    }

    // ==================== 内部方法 ====================

    private BigDecimal sumOrders(List<TradeOrder> list) {
        return list.stream()
                .map(o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumWallet(List<WalletTransaction> list) {
        return list.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}