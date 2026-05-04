package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class SysNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 接收用户ID，0=全部用户

    private String title;

    private String content;

    private Integer type; // 1=系统通知 2=订单提醒 3=账户变动 4=活动

    private Integer level; // 1=紧急 2=重要 3=一般

    private String linkUrl; // 点击跳转链接

    private Integer status; // 1=已读 0=未读

    private LocalDateTime readTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}