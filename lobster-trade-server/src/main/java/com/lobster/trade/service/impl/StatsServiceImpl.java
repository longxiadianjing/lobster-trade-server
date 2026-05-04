package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.mapper.ProductMapper;
import com.lobster.trade.mapper.TradeOrderMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.Product;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.service.DisputeService;
import com.lobster.trade.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final TradeOrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final DisputeService disputeService;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new LinkedHashMap<>();

        long totalUsers = userMapper.selectCount(null);
        result.put("totalUsers", totalUsers);

        long totalProducts = productMapper.selectCount(
            new LambdaQueryWrapper<Product>().eq(Product::getIsDeleted, 0)
        );
        result.put("totalProducts", totalProducts);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        LocalDateTime todayEnd = todayStart.plusDays(1);
        List<String> paidStatuses = List.of("paid", "in_progress", "submitted", "confirmed", "completed");

        LambdaQueryWrapper<TradeOrder> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(TradeOrder::getCreateTime, todayStart)
                   .lt(TradeOrder::getCreateTime, todayEnd);
        long todayOrders = orderMapper.selectCount(todayWrapper);
        result.put("todayOrders", todayOrders);

        LambdaQueryWrapper<TradeOrder> gmvWrapper = new LambdaQueryWrapper<>();
        gmvWrapper.ge(TradeOrder::getCreateTime, todayStart)
                  .lt(TradeOrder::getCreateTime, todayEnd)
                  .in(TradeOrder::getStatus, paidStatuses);
        List<TradeOrder> todayPaidOrders = orderMapper.selectList(gmvWrapper);
        BigDecimal todayGmv = todayPaidOrders.stream()
                .map(TradeOrder::getOrderAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todayGmv", todayGmv);
        result.put("todayGmvFormatted", formatGmv(todayGmv));

        result.put("orderTrend", getOrderTrend(7));

        long pendingDisputes = disputeService.countPending();
        long totalDisputes = disputeService.countTotal();
        result.put("pendingDisputes", pendingDisputes);
        result.put("totalDisputes", totalDisputes);

        LambdaQueryWrapper<TradeOrder> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.orderByDesc(TradeOrder::getCreateTime).last("LIMIT 5");
        List<TradeOrder> recentOrders = orderMapper.selectList(recentWrapper);
        List<Map<String, Object>> recentOrderList = recentOrders.stream().map(this::toOrderMap).collect(Collectors.toList());
        result.put("recentOrders", recentOrderList);

        return result;
    }

    private Map<String, Object> toOrderMap(TradeOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo());
        m.put("productTitle", o.getProductTitle());
        m.put("buyerId", o.getBuyerId());
        m.put("sellerId", o.getSellerId());
        m.put("orderAmount", o.getOrderAmount());
        m.put("status", o.getStatus());
        m.put("createTime", o.getCreateTime());
        return m;
    }

    @Override
    public List<Map<String, Object>> getOrderTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<String> paidStatuses = List.of("paid", "in_progress", "submitted", "confirmed", "completed");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            LambdaQueryWrapper<TradeOrder> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.ge(TradeOrder::getCreateTime, dayStart)
                       .lt(TradeOrder::getCreateTime, dayEnd);
            long count = orderMapper.selectCount(countWrapper);

            LambdaQueryWrapper<TradeOrder> gmvWrapper = new LambdaQueryWrapper<>();
            gmvWrapper.ge(TradeOrder::getCreateTime, dayStart)
                      .lt(TradeOrder::getCreateTime, dayEnd)
                      .in(TradeOrder::getStatus, paidStatuses);
            List<TradeOrder> orders = orderMapper.selectList(gmvWrapper);
            BigDecimal gmv = orders.stream()
                    .map(TradeOrder::getOrderAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new LinkedHashMap<>();
            dayData.put("label", date.format(fmt));
            dayData.put("count", count);
            dayData.put("gmv", gmv);
            trend.add(dayData);
        }
        return trend;
    }

    @Override
    public Map<String, Long> getOrderStatusDistribution() {
        List<TradeOrder> orders = orderMapper.selectList(null);
        return orders.stream()
                .collect(Collectors.groupingBy(TradeOrder::getStatus, Collectors.counting()));
    }

    @Override
    public List<Map<String, Object>> getUserTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, dayStart)
                    .lt(User::getCreateTime, dayEnd)
            );

            Map<String, Object> dayData = new LinkedHashMap<>();
            dayData.put("label", date.format(fmt));
            dayData.put("count", count);
            trend.add(dayData);
        }
        return trend;
    }

    @Override
    public List<Map<String, Object>> getTopProducts(int limit) {
        List<TradeOrder> orders = orderMapper.selectList(
            new LambdaQueryWrapper<TradeOrder>()
                .in(TradeOrder::getStatus, List.of("paid", "in_progress", "submitted", "confirmed", "completed"))
                .orderByDesc(TradeOrder::getCreateTime)
                .last("LIMIT 200")
        );

        Map<Long, List<TradeOrder>> byProduct = orders.stream()
                .collect(Collectors.groupingBy(TradeOrder::getProductId));

        return byProduct.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(limit)
                .map(e -> {
                    List<TradeOrder> po = e.getValue();
                    BigDecimal totalAmount = po.stream()
                            .map(TradeOrder::getOrderAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("productId", e.getKey());
                    m.put("productTitle", po.get(0).getProductTitle());
                    m.put("orderCount", po.size());
                    m.put("orderAmount", totalAmount);
                    return m;
                })
                .collect(Collectors.toList());
    }

    private String formatGmv(BigDecimal v) {
        if (v == null || v.compareTo(BigDecimal.ZERO) == 0) return "0.00";
        if (v.compareTo(new BigDecimal(10000)) >= 0) {
            return v.divide(new BigDecimal(10000), 1, RoundingMode.HALF_UP) + "万";
        }
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
