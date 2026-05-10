package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("price_history")
public class PriceHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 调价前价格 */
    private BigDecimal priceBefore;

    /** 调价后价格 */
    private BigDecimal priceAfter;

    /** 操作者类型：USER/SELLER/ADMIN/SYSTEM */
    private String operatorType;

    /** 操作者ID */
    private Long operatorId;

    /** 调价原因 */
    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
