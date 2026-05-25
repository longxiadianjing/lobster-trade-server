package com.lobster.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.common.Result;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.mapper.WalletTransactionMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.mapper.ProductMapper;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.entity.WalletTransaction;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.entity.AdminPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据导出接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class ExportController {

    private final TradeOrderMapper orderMapper;
    private final WalletTransactionMapper walletTransMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter DFT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 订单导出 ====================

    /**
     * 导出订单 CSV
     * GET /api/admin/export/orders
     * 支持参数：status, tradeType, keyword, startDate, endDate, page=1&size=10000
     */
    @GetMapping(value = "/orders", produces = "text/csv;charset=UTF-8")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public ResponseEntity<String> exportOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tradeType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10000") int size) {

        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null && !status.isEmpty(), TradeOrder::getStatus, status);
        wrapper.eq(tradeType != null && !tradeType.isEmpty(), TradeOrder::getTradeType, tradeType);
        wrapper.like(keyword != null && !keyword.isEmpty(), TradeOrder::getOrderNo, keyword);
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(TradeOrder::getCreateTime, LocalDate.parse(startDate, DATE).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(TradeOrder::getCreateTime, LocalDate.parse(endDate, DATE).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(TradeOrder::getCreateTime);
        wrapper.last("LIMIT " + size);

        List<TradeOrder> orders = orderMapper.selectList(wrapper);

        StringBuilder csv = new StringBuilder();
        // BOM for Excel GBK/UTF-8兼容
        csv.append("\uFEFF");
        csv.append("订单号,商品标题,交易类型,买家ID,卖家ID,订单金额,托管金额,卖家实收,订单状态,仲裁状态,游戏ID,区服,创建时间,支付时间,完成时间\n");

        for (TradeOrder o : orders) {
            csv.append(escapeCsv(o.getOrderNo())).append(",");
            csv.append(escapeCsv(o.getProductTitle())).append(",");
            csv.append(escapeCsv(o.getTradeType())).append(",");
            csv.append(escapeCsv(String.valueOf(o.getBuyerId()))).append(",");
            csv.append(escapeCsv(String.valueOf(o.getSellerId()))).append(",");
            csv.append(escapeCsv(o.getOrderAmount() != null ? o.getOrderAmount().toPlainString() : "")).append(",");
            csv.append(escapeCsv(o.getEscrowAmount() != null ? o.getEscrowAmount().toPlainString() : "")).append(",");
            csv.append(escapeCsv(o.getSellerReceived() != null ? o.getSellerReceived().toPlainString() : "")).append(",");
            csv.append(escapeCsv(o.getStatus())).append(",");
            csv.append(escapeCsv(disputeStatus(o.getDisputeStatus()))).append(",");
            csv.append(escapeCsv(o.getGameId() != null ? String.valueOf(o.getGameId()) : "")).append(",");
            csv.append(escapeCsv(o.getCreateTime() != null ? o.getCreateTime().format(DFT) : "")).append(",");
            csv.append(escapeCsv(o.getPaymentTime() != null ? o.getPaymentTime().format(DFT) : "")).append(",");
            csv.append(escapeCsv(o.getConfirmTime() != null ? o.getConfirmTime().format(DFT) : "")).append("\n");
        }

        String filename = "orders_" + LocalDate.now().format(DATE) + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(csv.toString());
    }

    // ==================== 财务统计 ====================

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

        // 订单统计
        BigDecimal todayGmv = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")
                        .ge(TradeOrder::getCreateTime, todayStart))
                .stream().map(o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weekGmv = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")
                        .ge(TradeOrder::getCreateTime, weekStart))
                .stream().map(o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthGmv = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")
                        .ge(TradeOrder::getCreateTime, monthStart))
                .stream().map(o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGmv = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed"))
                .stream().map(o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 充值/提现（只看 wallet_transaction type=1 收入, type=2 支出）
        BigDecimal todayRecharge = sumWalletTrans(walletTransMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, 1)
                        .eq(WalletTransaction::getStatus, 1)
                        .ge(WalletTransaction::getCreateTime, todayStart)), "amount");

        BigDecimal monthRecharge = sumWalletTrans(walletTransMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, 1)
                        .eq(WalletTransaction::getStatus, 1)
                        .ge(WalletTransaction::getCreateTime, monthStart)), "amount");

        BigDecimal todayWithdraw = sumWalletTrans(walletTransMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, 2)
                        .eq(WalletTransaction::getStatus, 1)
                        .ge(WalletTransaction::getCreateTime, todayStart)), "amount");

        BigDecimal monthWithdraw = sumWalletTrans(walletTransMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getType, 2)
                        .eq(WalletTransaction::getStatus, 1)
                        .ge(WalletTransaction::getCreateTime, monthStart)), "amount");

        // 平台收入（已完成订单的托管金额 - 卖家实收，统计 completed）
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

        // 订单数量
        long todayOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>()
                        .ge(TradeOrder::getCreateTime, todayStart));

        long monthOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>()
                        .ge(TradeOrder::getCreateTime, monthStart));

        // 用户数
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
     * 近30天每日 GMV 趋势
     * GET /api/admin/finance/gmv-trend
     */
    @GetMapping("/gmv-trend")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<List<Map<String, Object>>> gmvTrend(
            @RequestParam(defaultValue = "30") int days) {
        LocalDateTime start = nowLocal().toLocalDate().minusDays(days).atStartOfDay();
        List<TradeOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .in(TradeOrder::getStatus, "paid", "in_progress", "submitted", "confirmed", "completed")
                        .ge(TradeOrder::getCreateTime, start)
                        .orderByAsc(TradeOrder::getCreateTime));

        Map<LocalDate, BigDecimal> byDate = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO,
                                o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO,
                                BigDecimal::add)));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = nowLocal().toLocalDate().minusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.format(DATE));
            point.put("gmv", byDate.getOrDefault(d, BigDecimal.ZERO));
            result.add(point);
        }
        return Result.success(result);
    }

    /**
     * 近30天每日订单量趋势
     * GET /api/admin/finance/order-trend
     */
    @GetMapping("/order-trend")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<List<Map<String, Object>>> orderTrend(
            @RequestParam(defaultValue = "30") int days) {
        LocalDateTime start = nowLocal().toLocalDate().minusDays(days).atStartOfDay();
        List<TradeOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .ge(TradeOrder::getCreateTime, start)
                        .orderByAsc(TradeOrder::getCreateTime));

        Map<LocalDate, Long> byDate = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate(),
                        Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = nowLocal().toLocalDate().minusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.format(DATE));
            point.put("count", byDate.getOrDefault(d, 0L));
            result.add(point);
        }
        return Result.success(result);
    }

    /**
     * 交易类型分布
     * GET /api/admin/finance/trade-type-dist
     */
    @GetMapping("/trade-type-dist")
    @RequirePermission(AdminPermission.ORDER_VIEW)
    public Result<List<Map<String, Object>>> tradeTypeDist() {
        LocalDateTime monthStart = nowLocal().toLocalDate().minusDays(30).atStartOfDay();
        List<TradeOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .ge(TradeOrder::getCreateTime, monthStart));

        Map<String, Long> byType = orders.stream()
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

    private LocalDateTime nowLocal() {
        return LocalDateTime.now();
    }

    private BigDecimal sumWalletTrans(List<WalletTransaction> list, String field) {
        return list.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String disputeStatus(Integer ds) {
        if (ds == null) return "无";
        return switch (ds) {
            case 1 -> "处理中";
            case 2 -> "已处理";
            default -> "无";
        };
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}