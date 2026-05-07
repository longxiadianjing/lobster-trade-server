package com.lobster.trade.model.request;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class ProductPublishRequest {

    @NotNull(message = "请选择游戏")
    private Long gameId;

    @NotNull(message = "请选择分类")
    private Long categoryId;

    @NotBlank(message = "请选择商品类型")
    private String productType;   // boost/accompany/escort/goods

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多100字")
    private String title;

    private String description;

    private String images;        // JSON数组字符串

    @NotBlank(message = "请选择价格类型")
    private String priceType;     // fixed/per_wan/per_hour/per_game

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格最小为0.01")
    private BigDecimal price;

    private String unit;
    private String gameZone;
    private String server;
    private String platform;
    private BigDecimal minDeposit;
    private Integer estimatedHours;

    @Min(value = 1, message = "库存最小为1")
    private Integer stock;
}
