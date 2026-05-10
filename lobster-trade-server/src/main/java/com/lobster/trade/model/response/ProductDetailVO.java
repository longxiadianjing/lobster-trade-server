package com.lobster.trade.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailVO {
    private Long id;
    private Long sellerId;
    private Boolean isOfficial; // true if seller is admin (官方自营)
    private String sellerNickname;
    private BigDecimal sellerReputationScore;
    private Integer sellerIsVerified;
    private Integer sellerRealNameVerified;
    private Long gameId;
    private String gameName;
    private String productType;
    private String title;
    private String description;
    private String images;        // JSON数组字符串
    private String coverImage;    // 第一张图片URL（从images解析）
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
}
