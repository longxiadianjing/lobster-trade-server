package com.lobster.trade.model.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CsSessionVO {
    private Long id;
    private String sessionNo;
    private Long customerId;
    private String customerNickname;
    private Long operatorId;
    private String operatorNickname;
    private Long orderId;
    private String subject;
    private Integer status;
    private Integer priority;
    private String handlerName;
    private LocalDateTime createTime;
    private LocalDateTime closeTime;
    private List<CsMessageVO> messages;

    @Data
    public static class CsMessageVO {
        private Long id;
        private Long senderId;
        private Integer senderType;
        private String content;
        private String messageType;
        private String attachmentUrl;
        private LocalDateTime createTime;
        private Integer isRead;
    }
}
