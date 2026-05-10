package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dispute_ticket")
public class DisputeTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单号 */
    private String ticketNo;

    /** 关联订单ID */
    private Long orderId;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /** 纠纷类型：QUALITY/DELIVERY/PRICE/CHEAT/OTHER */
    private String disputeType;

    /** 买家描述 */
    private String disputeReason;

    /** 卖家申诉 */
    private String sellerDefense;

    /** 凭证图片URLs，JSON数组 */
    private String evidenceUrl;

    /** 管理员备注 */
    private String adminRemark;

    /** 处理人ID */
    private Long handlerId;

    /** 处理结果：BUYER_WIN/SELLER_WIN/REFUND/PARTIAL/CANCEL */
    private String result;

    /** 状态：0=待处理 1=处理中 2=已处理 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 处理时间 */
    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
