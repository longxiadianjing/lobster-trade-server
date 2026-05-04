package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("im_session")
public class ImSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionNo;
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadBuyer;
    private Integer unreadSeller;
    private Integer status;      // 1-正常 2-禁用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer isDeleted;
}
