package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_login_device")
public class UserLoginDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 设备指纹标识 */
    private String deviceFingerprint;

    /** 设备类型：PC/iOS/Android */
    private String deviceType;

    /** 设备名称/浏览器 */
    private String deviceName;

    /** IP 地址 */
    private String ipAddress;

    /** 登录地区（根据IP解析） */
    private String loginLocation;

    /** 操作系统 */
    private String osVersion;

    /** 浏览器版本 */
    private String browserVersion;

    /** 是否当前登录设备 */
    private Integer isCurrent;

    /** 是否可信设备（用户标记） */
    private Integer isTrusted;

    /** 最后活跃时间 */
    private LocalDateTime lastActiveTime;

    /** 首次登录时间 */
    private LocalDateTime firstLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}
