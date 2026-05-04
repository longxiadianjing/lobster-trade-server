package com.lobster.trade.model.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class SellerStatsResponse {
    private BigDecimal totalRevenue;     // 累计收入
    private BigDecimal todayRevenue;      // 今日收入
    private BigDecimal monthRevenue;      // 本月收入
    private Long totalOrders;            // 总订单数
    private Map<String, BigDecimal> recent7Days;  // 近7天每天收入
}
