package com.lobster.trade.model.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminProductVO {
    private Long id;
    private String title;
    private Long gameId;
    private String gameName;
    private Long categoryId;
    private String categoryName;
    private String productType;
    private String description;
    private String images;
    private String coverImage;
    private String priceType;
    private BigDecimal price;
    private String unit;
    private String gameZone;
    private String server;
    private String platform;
    private BigDecimal minDeposit;
    private Integer estimatedHours;
    private Integer stock;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer status;
    private LocalDateTime createTime;

    // 卖家信息
    private Long sellerId;
    private String sellerNickname;
    private String sellerPhone;
}
