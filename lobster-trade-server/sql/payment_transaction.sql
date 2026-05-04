-- 支付交易记录表
CREATE TABLE IF NOT EXISTS `payment_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID（充值时为null）',
    `payment_type` VARCHAR(20) NOT NULL COMMENT '支付类型：recharge-充值, order-订单支付',
    `amount` DECIMAL(12,2) NOT NULL COMMENT '支付金额',
    `channel` VARCHAR(20) NOT NULL COMMENT '支付渠道：alipay-支付宝, wechat-微信, bankcard-银行卡',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待支付, 1-支付成功, 2-支付失败, 3-已过期',
    `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '第三方交易流水号（Mock）',
    `paid_time` DATETIME DEFAULT NULL COMMENT '支付成功时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `error_msg` VARCHAR(255) DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付交易记录表';