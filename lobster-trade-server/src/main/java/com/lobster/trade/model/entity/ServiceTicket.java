package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("service_ticket")
public class ServiceTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String ticketNo; // 工单编号

    private String subject; // 工单主题

    private Integer category; // 1=交易纠纷 2=账户问题 3=商品咨询 4=功能建议 5=其他

    private Integer priority; // 1=紧急 2=高 3=中 4=低

    private Integer status; // 1=待处理 2=处理中 3=待用户确认 4=已解决 5=已关闭 6=已驳回

    private String description; // 用户描述

    private String images; // 图片JSON数组

    private String handlerReply; // 客服回复

    private Long handlerId; // 处理人ID

    private String handlerName;

    private LocalDateTime handleTime; // 处理时间

    private LocalDateTime closeTime;

    private Integer satisfaction; // 满意度评分 1-5

    private String userFeedback; // 用户反馈

    private LocalDateTime lastReplyTime; // 最后回复时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}