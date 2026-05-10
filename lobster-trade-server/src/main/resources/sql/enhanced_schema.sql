-- ============================================================
-- 龙虾道具交易平台 - 增强数据库建表脚本
-- 生成时间：2026-05-10
-- 说明：补充缺失的数据表，完善平台数据库设计
-- ============================================================

-- ------------------------------------------------
-- 1. bank_card_binding - 用户银行卡绑定表
-- ------------------------------------------------
DROP TABLE IF EXISTS `bank_card_binding`;
CREATE TABLE `bank_card_binding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `bank_name` VARCHAR(100) NOT NULL COMMENT '银行名称',
  `bank_branch` VARCHAR(200) DEFAULT '' COMMENT '开户支行',
  `card_number` VARCHAR(500) NOT NULL COMMENT '卡号（AES加密存储）',
  `card_number_last4` VARCHAR(4) NOT NULL COMMENT '卡号后4位',
  `holder_name` VARCHAR(100) NOT NULL COMMENT '持卡人姓名',
  `id_card_number` VARCHAR(500) DEFAULT '' COMMENT '身份证号（AES加密存储）',
  `id_card_number_last4` VARCHAR(4) DEFAULT '' COMMENT '身份证后4位',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '银行预留手机号',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待验证 1=已绑定 2=已解绑',
  `bind_time` DATETIME DEFAULT NULL COMMENT '绑定时间',
  `unbind_time` DATETIME DEFAULT NULL COMMENT '解绑时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删 1=已删',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户银行卡绑定表';

-- ------------------------------------------------
-- 2. withdraw_application - 提现申请表
-- ------------------------------------------------
DROP TABLE IF EXISTS `withdraw_application`;
CREATE TABLE `withdraw_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` VARCHAR(32) NOT NULL COMMENT '提现申请号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '申请金额',
  `fee` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '手续费',
  `actual_amount` DECIMAL(12,2) NOT NULL COMMENT '实到金额',
  `channel` VARCHAR(20) NOT NULL COMMENT '提现渠道：alipay/wechat/bank_card',
  `account` VARCHAR(200) NOT NULL COMMENT '账户信息（账号/卡号）',
  `account_name` VARCHAR(100) NOT NULL COMMENT '账户姓名',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待审核 1=处理中 2=已打款 3=已拒绝 4=已取消',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '管理员备注/拒绝原因',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `transaction_id` VARCHAR(64) DEFAULT '' COMMENT '打款交易号',
  `success_time` DATETIME DEFAULT NULL COMMENT '成功时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删 1=已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现申请表';

-- ------------------------------------------------
-- 3. risk_control_log - 风控操作日志表
-- ------------------------------------------------
DROP TABLE IF EXISTS `risk_control_log`;
CREATE TABLE `risk_control_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '触发用户ID',
  `event_type` VARCHAR(50) NOT NULL COMMENT '事件类型：RISK_LOGIN/RISK_ORDER/RISK_WITHDRAW/RISK_PAY/RISK_PRODUCT',
  `event_level` VARCHAR(20) NOT NULL COMMENT '事件级别：LOW/MEDIUM/HIGH/CRITICAL',
  `event_desc` VARCHAR(500) DEFAULT '' COMMENT '事件描述',
  `ip_address` VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
  `device_info` VARCHAR(500) DEFAULT '' COMMENT '设备信息',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数（JSON）',
  `handle_result` VARCHAR(20) DEFAULT 'PASS' COMMENT '处理结果：PASS/BLOCK/WARN',
  `handle_remark` VARCHAR(200) DEFAULT '' COMMENT '处理备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_event_level` (`event_level`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控操作日志表';

-- ------------------------------------------------
-- 4. user_login_log - 用户登录日志表
-- ------------------------------------------------
DROP TABLE IF EXISTS `user_login_log`;
CREATE TABLE `user_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（未注册时为null）',
  `username` VARCHAR(100) DEFAULT '' COMMENT '登录用户名',
  `ip_address` VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
  `device_type` VARCHAR(20) DEFAULT '' COMMENT '设备类型：PC/Android/iOS',
  `device_info` VARCHAR(500) DEFAULT '' COMMENT '设备详情',
  `browser` VARCHAR(200) DEFAULT '' COMMENT '浏览器版本',
  `os` VARCHAR(100) DEFAULT '' COMMENT '操作系统',
  `login_location` VARCHAR(200) DEFAULT '' COMMENT '登录地点',
  `result` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '登录结果：SUCCESS/FAILED',
  `fail_reason` VARCHAR(200) DEFAULT '' COMMENT '失败原因',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_ip_address` (`ip_address`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- ------------------------------------------------
-- 5. product_media - 商品媒体表（多图/视频）
-- ------------------------------------------------
DROP TABLE IF EXISTS `product_media`;
CREATE TABLE `product_media` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `media_type` VARCHAR(20) NOT NULL DEFAULT 'image' COMMENT '媒体类型：image/video',
  `url` VARCHAR(500) NOT NULL COMMENT '资源URL',
  `thumbnail_url` VARCHAR(500) DEFAULT '' COMMENT '缩略图URL',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
  `width` INT DEFAULT NULL COMMENT '图片宽度',
  `height` INT DEFAULT NULL COMMENT '图片高度',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删 1=已删',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品媒体表';

-- ------------------------------------------------
-- 6. escrow_transaction - 托管资金流水表
-- ------------------------------------------------
DROP TABLE IF EXISTS `escrow_transaction`;
CREATE TABLE `escrow_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `escrow_no` VARCHAR(32) NOT NULL COMMENT '托管流水号',
  `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
  `order_no` BIGINT DEFAULT NULL COMMENT '关联订单号',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '托管金额',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/FROZEN/RELEASED/REFUNDED',
  `action` VARCHAR(20) DEFAULT '' COMMENT '操作类型：FROZEN/RELEASE/REFUND/CANCEL',
  `balance_before` DECIMAL(12,2) DEFAULT NULL COMMENT '操作前余额',
  `balance_after` DECIMAL(12,2) DEFAULT NULL COMMENT '操作后余额',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_escrow_no` (`escrow_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='托管资金流水表';

-- ------------------------------------------------
-- 7. user_security_log - 用户安全操作日志表
-- ------------------------------------------------
DROP TABLE IF EXISTS `user_security_log`;
CREATE TABLE `user_security_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `action_type` VARCHAR(50) NOT NULL COMMENT '操作类型：PASSWORD_CHANGE/PAY_PASSWORD_SET/REAL_NAME/BIND_CARD/LOGIN',
  `ip_address` VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
  `device_info` VARCHAR(500) DEFAULT '' COMMENT '设备信息',
  `detail` VARCHAR(500) DEFAULT '' COMMENT '操作详情',
  `result` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILED',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户安全操作日志表';

-- ------------------------------------------------
-- 8. platform_config - 平台配置表
-- ------------------------------------------------
DROP TABLE IF EXISTS `platform_config`;
CREATE TABLE `platform_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `config_desc` VARCHAR(500) DEFAULT '' COMMENT '配置说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台配置表';

-- 初始化关键配置项
INSERT INTO `platform_config` (`config_key`, `config_value`, `config_name`, `config_desc`) VALUES
('commission_rate', '0.05', '平台佣金比例', '订单成交后平台收取的服务费比例，默认5%'),
('withdraw_fee_rate', '0.01', '提现手续费比例', '提现时收取的手续费比例，默认1%'),
('withdraw_fee_min', '1.00', '提现最低手续费', '提现手续费最低金额，单位元'),
('withdraw_amount_min', '10.00', '最低提现金额', '单次提现最低金额，单位元'),
('risk_max_daily_withdraw', '50000.00', '单日最高提现限额', '单个用户单日提现最高金额，单位元'),
('risk_max_daily_orders', '50', '单日最高下单数', '单个用户单日下单数量上限'),
('order_expire_minutes', '30', '订单支付过期时间', '订单创建后未支付自动取消的时间，单位分钟'),
('alipay_enabled', 'true', '支付宝支付开关', '是否启用支付宝支付渠道'),
('wechatpay_enabled', 'false', '微信支付开关', '是否启用微信支付渠道'),
('auto_audit_withdraw', 'false', '提现自动审核开关', '是否启用提现申请自动审核');
ALTER TABLE `platform_config` AUTO_INCREMENT=100;

-- ------------------------------------------------
-- 9. dispute_ticket - 交易纠纷表
-- ------------------------------------------------
DROP TABLE IF EXISTS `dispute_ticket`;
CREATE TABLE `dispute_ticket` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ticket_no` VARCHAR(32) NOT NULL COMMENT '工单号',
  `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `dispute_type` VARCHAR(30) NOT NULL COMMENT '纠纷类型：QUALITY/DELIVERY/PRICE/CHEAT/OTHER',
  `dispute_reason` TEXT DEFAULT NULL COMMENT '买家描述',
  `seller_defense` TEXT DEFAULT NULL COMMENT '卖家申诉',
  `evidence_url` TEXT DEFAULT NULL COMMENT '凭证图片URLs，JSON数组',
  `admin_remark` VARCHAR(500) DEFAULT '' COMMENT '管理员备注',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `result` VARCHAR(30) DEFAULT '' COMMENT '处理结果：BUYER_WIN/SELLER_WIN/REFUND/PARTIAL/CANCEL',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待处理 1=处理中 2=已处理',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删 1=已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_no` (`ticket_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易纠纷表';

-- ------------------------------------------------
-- 10. price_history - 商品价格历史记录表
-- ------------------------------------------------
DROP TABLE IF EXISTS `price_history`;
CREATE TABLE `price_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `price_before` DECIMAL(12,2) NOT NULL COMMENT '调价前价格',
  `price_after` DECIMAL(12,2) NOT NULL COMMENT '调价后价格',
  `operator_type` VARCHAR(20) NOT NULL COMMENT '操作者类型：USER/SELLER/ADMIN/SYSTEM',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作者ID',
  `reason` VARCHAR(500) DEFAULT '' COMMENT '调价原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品价格历史记录表';

-- ------------------------------------------------
-- 索引优化建议
-- ------------------------------------------------
-- 以下是对已有表的索引补充建议（如需执行，请在DBA指导下操作）

-- trade_order 补充索引（防慢查询）
-- CREATE INDEX idx_trade_order_status ON trade_order(status);
-- CREATE INDEX idx_trade_order_buyer ON trade_order(buyer_id, status);
-- CREATE INDEX idx_trade_order_seller ON trade_order(seller_id, status);
-- CREATE INDEX idx_trade_order_create_time ON trade_order(create_time);

-- user 补充索引
-- CREATE INDEX idx_user_phone ON user(phone);
-- CREATE INDEX idx_user_status ON user(status);

-- wallet_transaction 补充索引
-- CREATE INDEX idx_wallet_trans_user_type ON wallet_transaction(user_id, type);
-- CREATE INDEX idx_wallet_trans_source ON wallet_transaction(source, source_id);

-- product 补充索引
-- CREATE INDEX idx_product_seller ON product(seller_id, status);
-- CREATE INDEX idx_product_game_category ON product(game_id, category_id, status);

-- ------------------------------------------------
-- 外键关系说明
-- ------------------------------------------------
-- bank_card_binding.user_id -> user.id
-- withdraw_application.user_id -> user.id
-- withdraw_application.auditor_id -> admin.id
-- risk_control_log.user_id -> user.id (nullable)
-- user_login_log.user_id -> user.id (nullable)
-- product_media.product_id -> product.id
-- escrow_transaction.order_id -> trade_order.id
-- escrow_transaction.buyer_id -> user.id
-- escrow_transaction.seller_id -> user.id
-- user_security_log.user_id -> user.id
-- dispute_ticket.order_id -> trade_order.id
-- dispute_ticket.buyer_id -> user.id
-- dispute_ticket.seller_id -> user.id
-- dispute_ticket.handler_id -> admin.id
-- price_history.product_id -> product.id
