-- 支付交易记录表（Mock支付系统）
CREATE TABLE IF NOT EXISTS `payment_transaction` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID（充值时为null）',
  `payment_type` VARCHAR(20) NOT NULL COMMENT '支付类型：recharge-充值, order-订单支付',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '支付金额',
  `channel` VARCHAR(20) NOT NULL COMMENT '支付渠道：alipay-支付宝, wechat-微信, bankcard-银行卡',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待支付, 1-支付成功, 2-支付失败, 3-已过期',
  `transaction_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易流水号（Mock）',
  `paid_time` DATETIME DEFAULT NULL COMMENT '支付成功时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `error_msg` VARCHAR(255) DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX `idx_payment_no` (`payment_no`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付交易记录表';

-- 模拟支付成功的存储过程（用于测试）
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `mock_payment_success`(IN p_payment_no VARCHAR(64))
BEGIN
  UPDATE payment_transaction
  SET status = 1,
      transaction_id = CONCAT('MOCK_', UNIX_TIMESTAMP()),
      paid_time = NOW(),
      update_time = NOW()
  WHERE payment_no = p_payment_no AND status = 0;
END //
DELIMITER ;