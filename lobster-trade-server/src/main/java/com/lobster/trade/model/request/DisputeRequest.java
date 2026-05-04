package com.lobster.trade.model.request;

import lombok.Data;

@Data
public class DisputeRequest {
    private Long orderId;
    private String reason;
    private String evidence;  // 可选的证据说明
}