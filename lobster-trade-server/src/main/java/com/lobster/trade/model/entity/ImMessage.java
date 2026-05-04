package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("im_message")
public class ImMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private Long senderId;
    private String senderRole;   // buyer / seller / system
    private String messageType;   // text / image / file / system
    private String content;
    private String attachmentUrl;
    private Integer isRead;       // 0-未读 1-已读

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer isDeleted;
}
