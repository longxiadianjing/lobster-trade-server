package com.lobster.trade.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSimpleVO {
    private Long id;
    private String productTitle;
    private BigDecimal orderAmount;
    private LocalDateTime createTime;
}
