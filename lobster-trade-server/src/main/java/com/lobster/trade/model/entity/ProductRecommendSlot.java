package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_recommend_slot")
public class ProductRecommendSlot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 推荐位标识: home_banner/home_featured/product_detail_side */
    private String slotKey;

    /** 商品ID */
    private Long productId;

    /** 排序，越小越靠前 */
    private Integer sortOrder;

    /** 展示开始时间 */
    private LocalDateTime startTime;

    /** 展示结束时间 */
    private LocalDateTime endTime;

    /** 状态: 0=禁用 1=启用 */
    private Integer status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
