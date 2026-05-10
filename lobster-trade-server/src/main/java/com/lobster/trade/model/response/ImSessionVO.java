package com.lobster.trade.model.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ImSessionVO {
    private Long id;
    private String sessionNo;
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private String buyerNickname;
    private String sellerNickname;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadBuyer;
    private Integer unreadSeller;
    private Integer status;
    private LocalDateTime createTime;

    // 当前用户的未读数（动态计算）
    private Integer myUnread;

    // 关联订单简要信息
    private String orderNo;
    private String productTitle;
    private String orderStatus;

    // 消息列表（最近20条）
    private List<ImMessageVO> recentMessages;

    @Data
    public static class ImMessageVO {
        private Long id;
        private Long sessionId;
        private Long senderId;
        private String senderRole;
        private String senderNickname;
        private String messageType;
        private String content;
        private String attachmentUrl;
        private Integer isRead;
        private LocalDateTime createTime;
    }
}
