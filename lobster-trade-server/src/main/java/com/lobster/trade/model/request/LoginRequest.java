package com.lobster.trade.model.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String phone;
    private String password;
    /** 设备指纹（用于记录登录设备） */
    private String deviceFingerprint;
    /** 设备名称/浏览器信息 */
    private String deviceName;
    /** IP地址 */
    private String ipAddress;
}
