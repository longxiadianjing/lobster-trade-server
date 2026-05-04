package com.lobster.trade.model.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductPublishRequest {
    private Long gameId;
    private Long categoryId;
    private String productType;   // boost/accompany/escort/goods
    private String title;
    private String description;
    private String images;        // JSON数组字符串
    private String priceType;     // fixed/per_wan/per_hour/per_game
    private BigDecimal price;
    private String unit;
    private String gameZone;
    private String server;
    private String platform;
    private BigDecimal minDeposit;
    private Integer estimatedHours;
    private Integer stock;
}
