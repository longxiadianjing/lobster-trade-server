-- ================================================
-- 龙虾道具交易平台 - 数据库初始化脚本
-- Sprint 1: 用户系统 + 钱包
-- 创建时间: 2026-04-22
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `lobster_trade` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `lobster_trade`;

-- ================================================
-- 1. 用户表
-- ================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `password` VARCHAR(128) NOT NULL COMMENT '密码（加密）',
  `pay_password` VARCHAR(128) DEFAULT NULL COMMENT '支付密码',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
  `id_card_front` VARCHAR(255) DEFAULT NULL COMMENT '身份证正面',
  `id_card_back` VARCHAR(255) DEFAULT NULL COMMENT '身份证反面',
  `real_name_status` TINYINT DEFAULT 0 COMMENT '实名状态（0-未实名，1-已实名，2-审核中，3-未通过）',
  `user_level` INT DEFAULT 1 COMMENT '用户等级（1-普通，2-铜牌，3-银牌，4-金牌）',
  `reputation_score` DECIMAL(3,2) DEFAULT 5.00 COMMENT '信誉评分（1-5）',
  `total_trade_count` INT DEFAULT 0 COMMENT '累计交易次数',
  `total_trade_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计交易金额',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '钱包余额（元）',
  `frozen_balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '冻结金额（元）',
  `status` TINYINT DEFAULT 1 COMMENT '账号状态（1-正常，2-封禁，3-冻结）',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  `register_ip` VARCHAR(64) DEFAULT NULL COMMENT '注册IP',
  `register_source` VARCHAR(32) DEFAULT NULL COMMENT '注册来源（PC/H5/APP）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_real_name_status` (`real_name_status`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ================================================
-- 2. 钱包表
-- ================================================
CREATE TABLE IF NOT EXISTS `wallet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '钱包ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '余额（元）',
  `frozen_balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额（元）',
  `total_income` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计收入',
  `total_expense` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计支出',
  `password_set` TINYINT DEFAULT 0 COMMENT '是否设置支付密码（0-否，1-是）',
  `password` VARCHAR(128) DEFAULT NULL COMMENT '支付密码（加密）',
  `alipay_account` VARCHAR(64) DEFAULT NULL COMMENT '支付宝账号',
  `alipay_name` VARCHAR(64) DEFAULT NULL COMMENT '支付宝实名',
  `wechat_openid` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID',
  `bank_card_no` VARCHAR(32) DEFAULT NULL COMMENT '银行卡号',
  `bank_name` VARCHAR(64) DEFAULT NULL COMMENT '开户行',
  `bank_username` VARCHAR(64) DEFAULT NULL COMMENT '银行卡实名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包表';

-- ================================================
-- 3. 钱包流水表
-- ================================================
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `trans_no` VARCHAR(64) NOT NULL COMMENT '流水号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '类型（1-收入，2-支出，3-冻结，4-解冻，5-退款）',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '变动金额（元）',
  `balance_before` DECIMAL(12,2) NOT NULL COMMENT '变动前余额',
  `balance_after` DECIMAL(12,2) NOT NULL COMMENT '变动后余额',
  `frozen_before` DECIMAL(12,2) DEFAULT 0.00 COMMENT '变动前冻结金额',
  `frozen_after` DECIMAL(12,2) DEFAULT 0.00 COMMENT '变动后冻结金额',
  `source` VARCHAR(32) NOT NULL COMMENT '来源（order/recharge/withdraw/commission/refund）',
  `source_id` BIGINT DEFAULT NULL COMMENT '来源ID（订单ID/充值ID等）',
  `source_no` VARCHAR(64) DEFAULT NULL COMMENT '来源单号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1-成功，2-失败，3-处理中）',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trans_no` (`trans_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_source` (`source`,`source_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水表';

-- ================================================
-- 4. 短信验证码表（用于注册/登录/找回密码）
-- ================================================
CREATE TABLE IF NOT EXISTS `sms_code` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `code` VARCHAR(8) NOT NULL COMMENT '验证码',
  `type` VARCHAR(32) NOT NULL COMMENT '验证码类型（register/login/reset_password）',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `used` TINYINT DEFAULT 0 COMMENT '是否已使用（0-否，1-是）',
  `used_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_phone_type` (`phone`,`type`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';

-- ================================================
-- 5. 用户会话表（用于JWT黑名单/会话管理）
-- ================================================
CREATE TABLE IF NOT EXISTS `user_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `token` VARCHAR(512) NOT NULL COMMENT 'Token',
  `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_token` (`token`(255)),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话表';

-- ================================================
-- 6. 管理员表（平台运营用）
-- ================================================
CREATE TABLE IF NOT EXISTS `admin_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(64) NOT NULL COMMENT '管理员用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `role_ids` VARCHAR(128) NOT NULL COMMENT '角色ID列表（JSON数组）',
  `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态（1-正常，2-禁用）',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ================================================
-- 初始化测试数据
-- ================================================
-- 插入测试管理员（密码: admin123）
INSERT INTO `admin_user` (`username`, `password`, `real_name`, `role_ids`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', '[1]', 1);

-- ================================================
-- 说明文档
-- ================================================
-- Sprint 1 涉及的表: user, wallet, wallet_transaction, sms_code, user_session, admin_user
-- 其他业务表（game_category, product, trade_order, boost_order等）将在后续Sprint创建
