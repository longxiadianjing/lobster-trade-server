package com.lobster.trade.model.response;

import lombok.Data;

@Data
public class AdminVO {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private String permissions;
}