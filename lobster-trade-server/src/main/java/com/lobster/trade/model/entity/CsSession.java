package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cs_session")
public class CsSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionNo;
    private Long customerId;
    private Long operatorId;
    private Long orderId;
    private String subject;

    /** 0-等待中 1-进行中 2-已关闭 */
    private Integer status;

    /** 0-普通 1-紧急 */
    private Integer priority;

    private String handlerName;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime closeTime;

    @TableLogic
    private Integer isDeleted;
}
