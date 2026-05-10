package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_login_log")
public class UserLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（未注册时为null） */
    private Long userId;

    /** 用户名（登录时使用，可为手机号/邮箱） */
    private String username;

    /** IP地址 */
    private String ipAddress;

    /** 设备类型：PC/Android/iOS */
    private String deviceType;

    /** 设备详情 */
    private String deviceInfo;

    /** 浏览器版本 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 登录地点 */
    private String loginLocation;

    /** 登录结果：SUCCESS/FAILED */
    private String result;

    /** 失败原因 */
    private String failReason;

    /** 登录时间 */
    private LocalDateTime loginTime;
}
