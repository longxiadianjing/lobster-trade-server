package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("platform_config")
public class PlatformConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键（唯一），如 commission_rate / withdraw_fee_min */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置名称 */
    private String configName;

    /** 配置说明 */
    private String configDesc;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
