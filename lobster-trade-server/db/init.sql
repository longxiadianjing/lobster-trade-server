-- ============================================================
-- 龙虾道具交易平台 - init.sql
-- 完整的数据库建表脚本（从现有数据库导出）
-- 30张表，按依赖顺序排列
-- ============================================================

CREATE DATABASE IF NOT EXISTS `lobster_trade`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `lobster_trade`;

-- ============================================================
-- 1. admin_role （管理员角色表，无依赖）
-- ============================================================
CREATE TABLE IF NOT EXISTS `admin_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(32) NOT NULL COMMENT '角色代码',
  `role_name` varchar(64) NOT NULL COMMENT '角色名称',
  `permissions` varchar(1024) DEFAULT NULL COMMENT '权限列表，逗号分隔',
  `description` varchar(256) DEFAULT NULL COMMENT '角色描述',
  `status` int DEFAULT '1' COMMENT '状态（1-正常，0-禁用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员角色表';

-- ============================================================
-- 2. user （用户表，无依赖）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `nickname` varchar(64) NOT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(128) NOT NULL COMMENT '密码（加密）',
  `pay_password` varchar(128) DEFAULT NULL COMMENT '支付密码',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号',
  `id_card_front` varchar(255) DEFAULT NULL COMMENT '身份证正面',
  `id_card_back` varchar(255) DEFAULT NULL COMMENT '身份证反面',
  `real_name_status` tinyint DEFAULT '0' COMMENT '实名状态（0-未实名，1-已实名，2-审核中，3-未通过）',
  `user_level` int DEFAULT '1' COMMENT '用户等级（1-普通，2-铜牌，3-银牌，4-金牌）',
  `reputation_score` decimal(3,2) DEFAULT '5.00' COMMENT '信誉评分（1-5）',
  `total_trade_count` int DEFAULT '0' COMMENT '累计交易次数',
  `total_trade_amount` decimal(12,2) DEFAULT '0.00' COMMENT '累计交易金额',
  `balance` decimal(12,2) DEFAULT '0.00' COMMENT '钱包余额（元）',
  `frozen_balance` decimal(12,2) DEFAULT '0.00' COMMENT '冻结金额（元）',
  `status` tinyint DEFAULT '1' COMMENT '账号状态（1-正常，2-封禁，3-冻结）',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `register_ip` varchar(64) DEFAULT NULL COMMENT '注册IP',
  `register_source` varchar(32) DEFAULT NULL COMMENT '注册来源（PC/H5/APP）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  `is_verified` tinyint DEFAULT '0' COMMENT '是否已实名认证',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_real_name_status` (`real_name_status`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 3. user_session （用户会话表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `token` varchar(512) NOT NULL COMMENT 'Token',
  `device_info` varchar(255) DEFAULT NULL COMMENT '设备信息',
  `ip` varchar(64) DEFAULT NULL COMMENT '登录IP',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_token` (`token`(255)),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话表';

-- ============================================================
-- 4. user_login_device （用户登录设备表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_login_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_fingerprint` varchar(256) DEFAULT NULL COMMENT '设备指纹',
  `device_type` varchar(32) DEFAULT NULL COMMENT '设备类型（PC/iOS/Android）',
  `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称',
  `ip_address` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `login_location` varchar(128) DEFAULT NULL COMMENT '登录地点',
  `os_version` varchar(64) DEFAULT NULL COMMENT '操作系统版本',
  `browser_version` varchar(64) DEFAULT NULL COMMENT '浏览器版本',
  `is_current` tinyint DEFAULT '0' COMMENT '是否当前设备',
  `is_trusted` tinyint DEFAULT '0' COMMENT '是否信任设备',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  `first_login_time` datetime DEFAULT NULL COMMENT '首次登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_fingerprint` (`device_fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录设备表';

-- ============================================================
-- 5. user_real_name （用户实名表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_real_name` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实名记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `real_name` varchar(64) NOT NULL COMMENT '真实姓名',
  `id_card` varchar(20) NOT NULL COMMENT '身份证号',
  `id_card_front` varchar(255) DEFAULT NULL COMMENT '身份证正面',
  `id_card_back` varchar(255) DEFAULT NULL COMMENT '身份证反面',
  `status` tinyint DEFAULT '0' COMMENT '状态（0=审核中，1=已通过，2=未通过，3=已撤销）',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `aliyun_verify_token` varchar(256) DEFAULT NULL COMMENT '阿里云认证Token',
  `aliyun_verify_result` text COMMENT '阿里云认证结果',
  `verify_time` datetime DEFAULT NULL COMMENT '认证时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户实名表';

-- ============================================================
-- 6. wallet （钱包表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `wallet` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '钱包ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '余额（元）',
  `frozen_balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '冻结金额（元）',
  `total_income` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计收入',
  `total_expense` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计支出',
  `password_set` tinyint DEFAULT '0' COMMENT '是否设置支付密码（0-否，1-是）',
  `password` varchar(128) DEFAULT NULL COMMENT '支付密码（加密）',
  `alipay_account` varchar(64) DEFAULT NULL COMMENT '支付宝账号',
  `alipay_name` varchar(64) DEFAULT NULL COMMENT '支付宝实名',
  `wechat_openid` varchar(64) DEFAULT NULL COMMENT '微信OpenID',
  `bank_card_no` varchar(32) DEFAULT NULL COMMENT '银行卡号',
  `bank_name` varchar(64) DEFAULT NULL COMMENT '开户行',
  `bank_username` varchar(64) DEFAULT NULL COMMENT '银行卡实名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包表';

-- ============================================================
-- 7. wallet_transaction （钱包流水表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `trans_no` varchar(64) NOT NULL COMMENT '流水号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '类型（1-收入，2-支出，3-冻结，4-解冻，5-退款）',
  `amount` decimal(12,2) NOT NULL COMMENT '变动金额（元）',
  `balance_before` decimal(12,2) NOT NULL COMMENT '变动前余额',
  `balance_after` decimal(12,2) NOT NULL COMMENT '变动后余额',
  `frozen_before` decimal(12,2) DEFAULT '0.00' COMMENT '变动前冻结金额',
  `frozen_after` decimal(12,2) DEFAULT '0.00' COMMENT '变动后冻结金额',
  `source` varchar(32) NOT NULL COMMENT '来源（order/recharge/withdraw/commission/refund）',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID（订单ID/充值ID等）',
  `source_no` varchar(64) DEFAULT NULL COMMENT '来源单号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-成功，2-失败，3-处理中）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trans_no` (`trans_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_source` (`source`,`source_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水表';

-- ============================================================
-- 8. sms_code （短信验证码表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `sms_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `code` varchar(8) NOT NULL COMMENT '验证码',
  `type` varchar(32) NOT NULL COMMENT '验证码类型（register/login/reset_password）',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `used` tinyint DEFAULT '0' COMMENT '是否已使用（0-否，1-是）',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_phone_type` (`phone`,`type`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';

-- ============================================================
-- 9. admin （管理员表（老版本））
-- ============================================================
CREATE TABLE IF NOT EXISTS `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(64) NOT NULL COMMENT '管理员用户名',
  `password` varchar(128) NOT NULL COMMENT '密码',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `role` varchar(32) DEFAULT 'ADMIN' COMMENT '角色',
  `permissions` varchar(1024) DEFAULT '*' COMMENT '权限',
  `status` int DEFAULT '1' COMMENT '状态（1-正常，0-禁用）',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表（老版本）';

-- ============================================================
-- 10. admin_user （管理员表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(64) NOT NULL COMMENT '管理员用户名',
  `password` varchar(128) NOT NULL COMMENT '密码',
  `real_name` varchar(64) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `role_ids` varchar(128) NOT NULL COMMENT '角色ID列表（JSON数组）',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `status` tinyint DEFAULT '1' COMMENT '状态（1-正常，2-禁用）',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ============================================================
-- 11. admin_audit_log （管理员操作审计日志）
-- ============================================================
CREATE TABLE IF NOT EXISTS `admin_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` bigint DEFAULT NULL COMMENT '管理员ID',
  `admin_username` varchar(64) DEFAULT NULL COMMENT '管理员用户名',
  `action` varchar(64) DEFAULT NULL COMMENT '操作类型',
  `entity_type` varchar(64) DEFAULT NULL COMMENT '实体类型',
  `entity_id` bigint DEFAULT NULL COMMENT '实体ID',
  `detail` varchar(1000) DEFAULT NULL COMMENT '操作详情',
  `ip` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(512) DEFAULT NULL COMMENT 'User-Agent',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_action` (`action`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作审计日志';

-- ============================================================
-- 12. game （游戏表（旧版本））
-- ============================================================
CREATE TABLE IF NOT EXISTS `game` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '游戏ID',
  `game_name` varchar(128) NOT NULL COMMENT '游戏名称',
  `game_code` varchar(64) NOT NULL COMMENT '游戏代码',
  `game_type` varchar(32) DEFAULT 'pc端游' COMMENT '游戏类型',
  `description` varchar(512) DEFAULT NULL COMMENT '游戏描述',
  `sort_order` int DEFAULT '100' COMMENT '排序',
  `status` int DEFAULT '1' COMMENT '状态（1-上线，0-下线）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `game_code` (`game_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏表（旧版本）';

-- ============================================================
-- 13. game_category （游戏分类表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `game_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '游戏分类ID',
  `game_name` varchar(64) NOT NULL COMMENT '游戏名称（如：三角洲行动）',
  `game_code` varchar(32) NOT NULL COMMENT '游戏代码（如：delta_force）',
  `game_type` varchar(16) NOT NULL COMMENT '游戏类型（fps/moba/slr等）',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标URL',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序（越大越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态（1-上线，0-下线）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_game_code` (`game_code`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏分类表';

-- ============================================================
-- 14. product_category （商品服务分类表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `game_id` bigint NOT NULL COMMENT '所属游戏ID（关联game_category）',
  `name` varchar(32) NOT NULL COMMENT '分类名称（如：游戏币、代练、装备）',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_game_id` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品服务分类表';

-- ============================================================
-- 15. product （商品表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `seller_id` bigint NOT NULL COMMENT '卖家用户ID',
  `game_id` bigint NOT NULL COMMENT '游戏分类ID（关联game_category）',
  `category_id` bigint DEFAULT NULL COMMENT '服务类型分类ID（关联product_category）',
  `product_type` varchar(16) NOT NULL COMMENT '商品类型（boost/accompany/escort/goods）',
  `title` varchar(128) NOT NULL COMMENT '商品标题',
  `description` text COMMENT '商品详情描述',
  `images` varchar(1024) DEFAULT NULL COMMENT '商品图片（JSON数组，最多9张）',
  `price_type` varchar(16) NOT NULL COMMENT '定价类型（fixed/per_wan/per_hour/per_game）',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `unit` varchar(16) DEFAULT NULL COMMENT '单位（万哈夫币/小时/局）',
  `game_zone` varchar(64) DEFAULT NULL COMMENT '游戏区服（如：烽火/战场）',
  `server` varchar(64) DEFAULT NULL COMMENT '服务器',
  `platform` varchar(16) DEFAULT NULL COMMENT '平台（PC端/手游）',
  `min_deposit` decimal(10,2) DEFAULT '0.00' COMMENT '最低保证金',
  `estimated_hours` int DEFAULT NULL COMMENT '预计完成时长（小时）',
  `stock` int DEFAULT '1' COMMENT '库存（代练类通常为1）',
  `total_orders` int DEFAULT '0' COMMENT '累计已完成订单数',
  `completed_orders` int DEFAULT '0' COMMENT '累计完成订单数',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `favorite_count` int DEFAULT '0' COMMENT '收藏数',
  `status` tinyint DEFAULT '1' COMMENT '状态（1-上架，2-下架，3-违规封禁）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_game_id` (`game_id`),
  KEY `idx_product_type` (`product_type`),
  KEY `idx_status` (`status`),
  KEY `idx_price` (`price`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================================
-- 16. trade_order （订单表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `trade_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `trade_type` varchar(16) NOT NULL COMMENT '交易类型（boost/accompany/escort/goods）',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_title` varchar(128) NOT NULL COMMENT '商品标题（冗余）',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `game_id` bigint NOT NULL COMMENT '游戏ID',
  `category_id` bigint DEFAULT NULL COMMENT '服务分类ID',
  `order_amount` decimal(10,2) NOT NULL COMMENT '订单总价',
  `deposit_seller` decimal(10,2) DEFAULT '0.00' COMMENT '卖家保证金',
  `deposit_buyer` decimal(10,2) DEFAULT '0.00' COMMENT '买家保证金',
  `commission_rate` decimal(5,4) DEFAULT '0.0500' COMMENT '平台手续费率（默认0.05）',
  `platform_fee` decimal(10,2) DEFAULT '0.00' COMMENT '平台手续费',
  `seller_received` decimal(10,2) DEFAULT '0.00' COMMENT '卖家实收金额',
  `escrow_amount` decimal(10,2) NOT NULL COMMENT '托管金额',
  `escrow_status` tinyint DEFAULT '1' COMMENT '托管状态（1-未托管，2-已托管，3-已释放，4-已退款）',
  `payment_status` tinyint DEFAULT '0' COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `payment_method` varchar(32) DEFAULT NULL COMMENT '支付方式（wallet/alipay/wechat）',
  `status` varchar(16) NOT NULL DEFAULT 'pending_pay' COMMENT '订单状态',
  `boost_requirement` text COMMENT '代练要求描述（买家填写的具体需求）',
  `start_time` datetime DEFAULT NULL COMMENT '代练开始时间',
  `estimated_complete_time` datetime DEFAULT NULL COMMENT '预计完成时间',
  `actual_complete_time` datetime DEFAULT NULL COMMENT '实际完成时间',
  `submit_time` datetime DEFAULT NULL COMMENT '卖家提交验收时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '买家确认时间',
  `delivery_images` varchar(1024) DEFAULT NULL COMMENT '发货凭证/截图（JSON数组）',
  `delivery_remark` varchar(255) DEFAULT NULL COMMENT '发货备注',
  `buyer_cancel` tinyint DEFAULT '0' COMMENT '买家是否取消过（0-否，1-是）',
  `refund_request` tinyint DEFAULT '0' COMMENT '退款申请（0-无，1-申请中，2-已退款）',
  `refund_reason` varchar(255) DEFAULT NULL COMMENT '退款原因',
  `dispute_status` tinyint DEFAULT '0' COMMENT '仲裁状态（0-无仲裁，1-仲裁中，2-仲裁完成）',
  `dispute_reason` text COMMENT '仲裁原因',
  `dispute_result` varchar(255) DEFAULT NULL COMMENT '仲裁结果',
  `dispute_time` datetime DEFAULT NULL COMMENT '仲裁时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_game_id` (`game_id`),
  KEY `idx_status` (`status`),
  KEY `idx_trade_type` (`trade_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 17. order_progress （订单进度记录表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `order_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `progress_percent` int NOT NULL COMMENT '进度百分比（0-100）',
  `progress_note` varchar(255) DEFAULT NULL COMMENT '进度说明（如：已完成第3章任务）',
  `screenshots` varchar(1024) DEFAULT NULL COMMENT '进度截图（JSON数组）',
  `evidence_type` varchar(16) DEFAULT NULL COMMENT '证据类型（screenshot/video）',
  `seller_submit` tinyint DEFAULT '1' COMMENT '卖家是否已提交（0-否，1-是）',
  `seller_submit_time` datetime DEFAULT NULL COMMENT '卖家提交时间',
  `buyer_ack` tinyint DEFAULT '0' COMMENT '买家是否确认（0-否，1-是）',
  `buyer_ack_time` datetime DEFAULT NULL COMMENT '买家确认时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单进度记录表';

-- ============================================================
-- 18. trade_review （评价表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `trade_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `reviewer_id` bigint DEFAULT NULL COMMENT '评价方用户ID',
  `reviewed_id` bigint DEFAULT NULL COMMENT '被评价方用户ID',
  `role` tinyint DEFAULT NULL COMMENT '评价角色（0-买家评卖家，1-卖家评买家）',
  `rating` tinyint DEFAULT NULL COMMENT '综合评分（1-5星）',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `is_anonymous` tinyint DEFAULT '0' COMMENT '是否匿名评价',
  `is_hidden` tinyint DEFAULT '0' COMMENT '是否隐藏评价',
  `reply_content` text COMMENT '商家回复内容',
  `replier_id` bigint DEFAULT NULL COMMENT '回复人ID',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_reviewer_id` (`reviewer_id`),
  KEY `idx_reviewed_id` (`reviewed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ============================================================
-- 19. im_session （IM会话表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `im_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_no` varchar(32) NOT NULL COMMENT '会话编号',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `last_message` varchar(255) DEFAULT NULL COMMENT '最后一条消息摘要',
  `last_message_at` datetime DEFAULT NULL COMMENT '最后消息时间',
  `unread_buyer` int DEFAULT '0' COMMENT '买家未读数',
  `unread_seller` int DEFAULT '0' COMMENT '卖家未读数',
  `status` tinyint DEFAULT '1' COMMENT '状态（1-正常，2-禁用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_no` (`session_no`),
  UNIQUE KEY `order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM会话表';

-- ============================================================
-- 20. im_message （IM消息表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `im_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `sender_role` varchar(10) NOT NULL COMMENT '发送者角色（buyer/seller/system）',
  `message_type` varchar(20) DEFAULT 'text' COMMENT '消息类型（text/image/file/system）',
  `content` text NOT NULL COMMENT '消息内容',
  `attachment_url` varchar(500) DEFAULT NULL COMMENT '附件URL',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读（0-未读，1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM消息表';

-- ============================================================
-- 21. cs_session （客服会话表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `cs_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_no` varchar(32) NOT NULL COMMENT '会话编号',
  `customer_id` bigint NOT NULL COMMENT '客户ID（用户）',
  `operator_id` bigint DEFAULT NULL COMMENT '客服人员ID',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID（可选）',
  `subject` varchar(255) DEFAULT NULL COMMENT '会话主题',
  `status` tinyint DEFAULT '0' COMMENT '状态（0=等待中，1=进行中，2=已关闭）',
  `priority` tinyint DEFAULT '0' COMMENT '优先级（0=普通，1=紧急）',
  `handler_name` varchar(64) DEFAULT NULL COMMENT '处理人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_no` (`session_no`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话表';

-- ============================================================
-- 22. cs_message （客服消息表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `cs_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `sender_type` tinyint DEFAULT '0' COMMENT '发送者类型（0=用户，1=客服）',
  `content` text NOT NULL COMMENT '消息内容',
  `message_type` varchar(20) DEFAULT 'text' COMMENT '消息类型（text/image/file）',
  `attachment_url` varchar(500) DEFAULT NULL COMMENT '附件URL',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读（0-未读，1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';

-- ============================================================
-- 23. coupon （优惠券表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` varchar(128) NOT NULL COMMENT '优惠券名称',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `type` tinyint NOT NULL COMMENT '类型（1=满减，2=折扣，3=充值赠送）',
  `min_amount` decimal(10,2) DEFAULT NULL COMMENT '满减门槛金额',
  `discount_value` decimal(10,2) DEFAULT NULL COMMENT '优惠金额',
  `discount_rate` decimal(5,4) DEFAULT NULL COMMENT '折扣率',
  `recharge_amount` decimal(10,2) DEFAULT NULL COMMENT '充值金额',
  `gift_amount` decimal(10,2) DEFAULT NULL COMMENT '赠送金额',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '发行总量',
  `issued_count` int NOT NULL DEFAULT '0' COMMENT '已发放数量',
  `per_user_limit` int NOT NULL DEFAULT '1' COMMENT '每人限领数量',
  `use_min_amount` decimal(10,2) DEFAULT NULL COMMENT '使用门槛金额',
  `start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `status` tinyint DEFAULT '1' COMMENT '状态（0=禁用，1=启用）',
  `scope` tinyint DEFAULT '0' COMMENT '适用范围（0=全场，1=充值，2=商品购买）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- ============================================================
-- 24. user_coupon （用户优惠券表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `receive_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint DEFAULT NULL COMMENT '使用订单ID',
  `status` tinyint DEFAULT '0' COMMENT '状态（0=未使用，1=已使用，2=已过期）',
  `coupon_name` varchar(128) DEFAULT NULL COMMENT '优惠券名称（冗余）',
  `discount_value` decimal(10,2) DEFAULT NULL COMMENT '优惠金额（冗余）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ============================================================
-- 25. hot_search_word （热搜词表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `hot_search_word` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '热搜词ID',
  `word` varchar(100) NOT NULL COMMENT '热搜词',
  `search_count` int DEFAULT '0' COMMENT '搜索次数',
  `sort_order` int DEFAULT '100' COMMENT '排序顺序（越小越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0=禁用，1=启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜词表';

-- ============================================================
-- 26. platform_announcement （平台公告表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `platform_announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(255) NOT NULL COMMENT '公告标题',
  `content` text COMMENT '公告内容',
  `type` tinyint DEFAULT '1' COMMENT '类型',
  `priority` tinyint DEFAULT '3' COMMENT '优先级',
  `status` tinyint DEFAULT '0' COMMENT '状态（0-草稿，1-已发布）',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台公告表';

-- ============================================================
-- 27. sys_notification （系统通知表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint DEFAULT '0' COMMENT '用户ID（0=全体用户）',
  `title` varchar(255) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `type` tinyint DEFAULT '1' COMMENT '类型',
  `level` tinyint DEFAULT '3' COMMENT '级别',
  `link_url` varchar(512) DEFAULT NULL COMMENT '链接URL',
  `status` tinyint DEFAULT '0' COMMENT '状态',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- ============================================================
-- 28. service_provider_certification （服务商认证表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `service_provider_certification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `certification_type` varchar(20) NOT NULL COMMENT '认证类型（boost=代练，accompany=陪玩）',
  `game_id` bigint DEFAULT NULL COMMENT '主要游戏ID',
  `service_description` varchar(500) DEFAULT NULL COMMENT '服务描述',
  `service_regions` varchar(200) DEFAULT NULL COMMENT '服务区服（多个用逗号分隔）',
  `hourly_rate` decimal(10,2) DEFAULT NULL COMMENT '参考时价（元/小时）',
  `credentials` varchar(1000) DEFAULT NULL COMMENT '资质证明图片URLs（JSON数组）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-待审核，1-通过，2-拒绝，3-冻结）',
  `provider_level` int DEFAULT '1' COMMENT '服务商等级',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `admin_remark` varchar(500) DEFAULT NULL COMMENT '管理员备注',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `expire_time` datetime DEFAULT NULL COMMENT '认证有效期截止时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务商认证表';

-- ============================================================
-- 29. service_ticket （客服工单表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `service_ticket` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `user_id` bigint DEFAULT NULL COMMENT '提交用户ID',
  `ticket_no` varchar(32) DEFAULT NULL COMMENT '工单编号',
  `subject` varchar(255) NOT NULL COMMENT '工单主题',
  `category` tinyint DEFAULT '5' COMMENT '工单分类',
  `priority` tinyint DEFAULT '3' COMMENT '优先级',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `description` text COMMENT '工单描述',
  `images` varchar(2000) DEFAULT NULL COMMENT '图片凭证',
  `handler_reply` text COMMENT '处理回复',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handler_name` varchar(64) DEFAULT NULL COMMENT '处理人姓名',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `satisfaction` tinyint DEFAULT NULL COMMENT '满意度评分',
  `user_feedback` varchar(500) DEFAULT NULL COMMENT '用户反馈',
  `last_reply_time` datetime DEFAULT NULL COMMENT '最后回复时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服工单表';

-- ============================================================
-- 30. product_recommend_slot （商品推荐位表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `product_recommend_slot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '推荐位ID',
  `slot_key` varchar(64) NOT NULL COMMENT '推荐位标识（home_banner/home_featured/product_detail_side）',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sort_order` int DEFAULT '0' COMMENT '排序（越小越靠前）',
  `start_time` datetime DEFAULT NULL COMMENT '展示开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '展示结束时间',
  `status` tinyint DEFAULT '1' COMMENT '状态（0=禁用，1=启用）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_slot_key` (`slot_key`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品推荐位表';
