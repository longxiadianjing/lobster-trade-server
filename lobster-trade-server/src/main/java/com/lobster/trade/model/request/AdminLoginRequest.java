package com.lobster.trade.model.request;

import lombok.Data;

@Data
public class AdminLoginRequest {
    private String username;
    private String password;
}