package com.lobster.trade.model.request;

import lombok.Data;

@Data
public class ImSendMessageRequest {
    private Long sessionId;
    private String content;
    private String messageType; // text/image/file
    private String attachmentUrl;
}
