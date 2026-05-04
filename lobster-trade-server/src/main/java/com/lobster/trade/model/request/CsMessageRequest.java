package com.lobster.trade.model.request;

import lombok.Data;

@Data
public class CsMessageRequest {
    private Long sessionId;
    private String content;
    private String messageType = "text";
    private String attachmentUrl;
}