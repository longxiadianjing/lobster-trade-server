package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("product")
public class Product {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;
    private Long gameId;
    private Long categoryId;
    private String productType;
    private String title;
    private String description;
    private String images;        // JSON数组字符串
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer isDeleted;

    // 瞬态字段，不对应数据库列，用于返回给前端
    @TableField(exist = false)
    private String coverImage;

    @TableField(exist = false)
    private Boolean isOfficial;

    /** 从 images JSON 数组中解析出封面图URL */
    public void computeCoverImage() {
        if (this.images == null || this.images.isEmpty()) {
            return;
        }
        try {
            List<String> list = MAPPER.readValue(this.images, List.class);
            if (list != null && !list.isEmpty()) {
                this.coverImage = list.get(0);
            }
        } catch (Exception e) {
            // 解析失败，coverImage 留空
        }
    }
}