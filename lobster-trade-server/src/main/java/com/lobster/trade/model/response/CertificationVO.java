package com.lobster.trade.model.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CertificationVO {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String certificationType;
    private Long gameId;
    private String gameName;
    private String serviceDescription;
    private String serviceRegions;
    private BigDecimal hourlyRate;
    private Integer status;
    private String rejectReason;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private LocalDateTime expireTime;

    // 用户统计
    private BigDecimal reputationScore;
    private Integer totalTradeCount;
    private Integer userLevel;
}
