package com.lobster.trade.model.request;

import lombok.Data;

@Data
public class CsStartRequest {
    private Long orderId;
    private String subject;
    private Integer priority;
}