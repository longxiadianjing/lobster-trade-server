package com.lobster.trade.model.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminProductUpdateRequest {
    private String title;
    private String description;
    private String images;
    private BigDecimal price;
    private String unit;
    private String priceType;
    private String gameZone;
    private String server;
    private String platform;
    private BigDecimal minDeposit;
    private Integer stock;
    private Integer estimatedHours;
    private Integer status;
}
