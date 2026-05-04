package com.lobster.trade.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StatsService {

    /** 概览统计 */
    Map<String, Object> getOverview();

    /** 近N天每日订单量和GMV */
    List<Map<String, Object>> getOrderTrend(int days);

    /** 订单状态分布 */
    Map<String, Long> getOrderStatusDistribution();

    /** 近N天每日新增用户数 */
    List<Map<String, Object>> getUserTrend(int days);

    /** 热销商品TOP N */
    List<Map<String, Object>> getTopProducts(int limit);
}
