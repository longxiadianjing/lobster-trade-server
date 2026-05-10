package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_media")
public class ProductMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 媒体类型：image/video */
    private String mediaType;

    /** 资源URL */
    private String url;

    /** 缩略图URL */
    private String thumbnailUrl;

    /** 排序顺序 */
    private Integer sortOrder;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}
