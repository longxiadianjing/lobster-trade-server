package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("risk_control_log")
public class RiskControlLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 触发用户 */
    private Long userId;

    /** 事件类型：RISK_LOGIN/RISK_ORDER/RISK_WITHDRAW/RISK_PAY/RISK_PRODUCT */
    private String eventType;

    /** 事件级别：LOW/MEDIUM/HIGH/CRITICAL */
    private String eventLevel;

    /** 事件描述 */
    private String eventDesc;

    /** IP地址 */
    private String ipAddress;

    /** 设备信息 */
    private String deviceInfo;

    /** 请求参数（JSON） */
    private String requestParams;

    /** 处理结果：PASS/BLOCK/WARN */
    private String handleResult;

    /** 处理备注 */
    private String handleRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
