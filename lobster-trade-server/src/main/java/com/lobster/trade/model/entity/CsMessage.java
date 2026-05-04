package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cs_message")
public class CsMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private Long senderId;

    /** 0-用户 1-客服 */
    private Integer senderType;

    private String content;

    /** text/image/file */
    private String messageType;

    private String attachmentUrl;

    private LocalDateTime createTime;
    private Integer isRead;

    @TableLogic
    private Integer isDeleted;
}