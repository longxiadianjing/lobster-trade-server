package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_provider_certification")
public class ServiceProviderCertification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 认证类型：boost(代练), accompany(陪玩) */
    private String certificationType;

    /** 主要游戏ID */
    private Long gameId;

    /** 服务描述 */
    private String serviceDescription;

    /** 服务区服（多个用逗号分隔） */
    private String serviceRegions;

    /** 参考时价（元/小时） */
    private BigDecimal hourlyRate;

    /** 服务商等级：1普通 2铜牌 3银牌 4金牌 */
    private Integer providerLevel = 1;

    /** 资质证明图片URLs，JSON数组 */
    private String credentials;

    /** 状态：0待审核，1通过，2拒绝，3冻结 */
    private Integer status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 管理员备注 */
    private String adminRemark;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    /** 认证有效期截止时间 */
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
