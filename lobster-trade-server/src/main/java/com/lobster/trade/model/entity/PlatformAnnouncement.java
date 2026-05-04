package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("platform_announcement")
public class PlatformAnnouncement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Integer type; // 1=系统公告 2=活动通知 3=维护通知 4=版本更新

    private Integer priority; // 1=紧急 2=重要 3=一般

    private Integer status; // 1=发布 0=草稿

    private LocalDateTime publishTime;

    private LocalDateTime endTime;

    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}