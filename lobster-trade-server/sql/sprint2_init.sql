-- ================================================
-- 龙虾道具交易平台 - Sprint 2 数据库扩展
-- 内容：游戏分类 · 商品 · 订单 · 进度 · 评价
-- 创建时间: 2026-04-22
-- ================================================

USE `lobster_trade`;

-- ================================================
-- 7. 游戏分类表
-- ================================================
CREATE TABLE IF NOT EXISTS `game_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '游戏分类ID',
  `game_name` VARCHAR(64) NOT NULL COMMENT '游戏名称（如：三角洲行动）',
  `game_code` VARCHAR(32) NOT NULL COMMENT '游戏代码（如：delta_force）',
  `game_type` VARCHAR(16) NOT NULL COMMENT '游戏类型（pc端游/mobile手游）',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序（越大越靠前）',
  `status` TINYINT DEFAULT 1 COMMENT '状态（1-上线，0-下线）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_game_code` (`game_code`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏分类表';

-- ================================================
-- 8. 商品表（代练/陪玩服务发布）
-- ================================================
CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家用户ID',
  `game_id` BIGINT NOT NULL COMMENT '游戏分类ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '服务类型分类ID（关联service_category）',
  `product_type` VARCHAR(16) NOT NULL COMMENT '商品类型（boost/p accompany/escort）',
  `title` VARCHAR(128) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品详情描述',
  `images` VARCHAR(1024) DEFAULT NULL COMMENT '商品图片（JSON数组，最多9张）',
  `price_type` VARCHAR(16) NOT NULL COMMENT '定价类型（fixed/per_wan/per_hour/per_game）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `unit` VARCHAR(16) DEFAULT NULL COMMENT '单位（万哈夫币/小时/局）',
  `game_zone` VARCHAR(64) DEFAULT NULL COMMENT '游戏区服（如：烽火/战场）',
  `server` VARCHAR(64) DEFAULT NULL COMMENT '服务器',
  `platform` VARCHAR(16) DEFAULT NULL COMMENT '平台（PC端/手游）',
  `min_deposit` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低保证金',
  `estimated_hours` INT DEFAULT NULL COMMENT '预计完成时长（小时）',
  `stock` INT DEFAULT 1 COMMENT '库存（代练类通常为1）',
  `total_orders` INT DEFAULT 0 COMMENT '累计已完成订单数',
  `completed_orders` INT DEFAULT 0 COMMENT '累计完成订单数',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏数',
  `status` TINYINT DEFAULT 1 COMMENT '状态（1-上架，2-下架，3-违规封禁）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_game_id` (`game_id`),
  KEY `idx_product_type` (`product_type`),
  KEY `idx_status` (`status`),
  KEY `idx_price` (`price`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ================================================
-- 9. 订单表（核心交易订单）
-- ================================================
CREATE TABLE IF NOT EXISTS `trade_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `trade_type` VARCHAR(16) NOT NULL COMMENT '交易类型（boost/accompany/escort/goods）',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_title` VARCHAR(128) NOT NULL COMMENT '商品标题（冗余）',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `game_id` BIGINT NOT NULL COMMENT '游戏ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '服务分类ID',

  -- 金额相关
  `order_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总价',
  `deposit_seller` DECIMAL(10,2) DEFAULT 0.00 COMMENT '卖家保证金',
  `deposit_buyer` DECIMAL(10,2) DEFAULT 0.00 COMMENT '买家保证金',
  `commission_rate` DECIMAL(5,4) DEFAULT 0.0500 COMMENT '平台手续费率（默认0.05）',
  `platform_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '平台手续费',
  `seller_received` DECIMAL(10,2) DEFAULT 0.00 COMMENT '卖家实收金额',

  -- 金额托管（买家先付款，钱在平台）
  `escrow_amount` DECIMAL(10,2) NOT NULL COMMENT '托管金额',
  `escrow_status` TINYINT DEFAULT 1 COMMENT '托管状态（1-未托管，2-已托管，3-已释放，4-已退款）',

  -- 支付相关
  `payment_status` TINYINT DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
  `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `payment_method` VARCHAR(32) DEFAULT NULL COMMENT '支付方式（wallet/alipay/wechat）',

  -- 交易状态
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending_pay' COMMENT '订单状态',
  -- pending_pay(待付款) → paid(已付款) → in_progress(进行中) → submitted(已提交验收) → confirmed(已确认) → completed(已完成) / disputed(仲裁中) / cancelled(已取消)

  -- 代练相关
  `boost_requirement` TEXT COMMENT '代练要求描述（买家填写的具体需求）',
  `start_time` DATETIME DEFAULT NULL COMMENT '代练开始时间',
  `estimated_complete_time` DATETIME DEFAULT NULL COMMENT '预计完成时间',
  `actual_complete_time` DATETIME DEFAULT NULL COMMENT '实际完成时间',
  `submit_time` DATETIME DEFAULT NULL COMMENT '卖家提交验收时间',
  `confirm_time` DATETIME DEFAULT NULL COMMENT '买家确认时间',

  -- 证据
  `delivery_images` VARCHAR(1024) DEFAULT NULL COMMENT '发货凭证/截图（JSON数组）',
  `delivery_remark` VARCHAR(255) DEFAULT NULL COMMENT '发货备注',

  -- 买家操作
  `buyer_cancel` TINYINT DEFAULT 0 COMMENT '买家是否取消过（0-否，1-是）',
  `refund_request` TINYINT DEFAULT 0 COMMENT '退款申请（0-无，1-申请中，2-已退款）',
  `refund_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',

  -- 仲裁
  `dispute_status` TINYINT DEFAULT 0 COMMENT '仲裁状态（0-无仲裁，1-仲裁中，2-仲裁完成）',
  `dispute_reason` TEXT COMMENT '仲裁原因',
  `dispute_result` VARCHAR(255) DEFAULT NULL COMMENT '仲裁结果',
  `dispute_time` DATETIME DEFAULT NULL COMMENT '仲裁时间',

  -- 时间戳
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
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

-- ================================================
-- 10. 订单进度记录表（代练进行中里程碑）
-- ================================================
CREATE TABLE IF NOT EXISTS `order_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `progress_percent` INT NOT NULL COMMENT '进度百分比（0-100）',
  `progress_note` VARCHAR(255) DEFAULT NULL COMMENT '进度说明（如：已完成第3章任务）',
  `screenshots` VARCHAR(1024) DEFAULT NULL COMMENT '进度截图（JSON数组）',
  `evidence_type` VARCHAR(16) DEFAULT NULL COMMENT '证据类型（screenshot/video）',
  `seller_submit` TINYINT DEFAULT 1 COMMENT '卖家是否已提交（0-否，1-是）',
  `seller_submit_time` DATETIME DEFAULT NULL COMMENT '卖家提交时间',
  `buyer_ack` TINYINT DEFAULT 0 COMMENT '买家是否确认（0-否，1-是）',
  `buyer_ack_time` DATETIME DEFAULT NULL COMMENT '买家确认时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单进度记录表';

-- ================================================
-- 11. 评价表（双向评价）
-- ================================================
CREATE TABLE IF NOT EXISTS `trade_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `from_user_id` BIGINT NOT NULL COMMENT '评价方用户ID',
  `to_user_id` BIGINT NOT NULL COMMENT '被评价方用户ID',
  `trade_type` VARCHAR(16) NOT NULL COMMENT '交易类型',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',

  -- 评分（1-5星）
  `rating` TINYINT NOT NULL COMMENT '综合评分',
  `rating_speed` TINYINT DEFAULT NULL COMMENT '速度评分',
  `rating_quality` TINYINT DEFAULT NULL COMMENT '质量评分',
  `rating_service` TINYINT DEFAULT NULL COMMENT '服务态度评分',

  `content` VARCHAR(512) DEFAULT NULL COMMENT '评价内容',
  `tags` VARCHAR(256) DEFAULT NULL COMMENT '评价标签（JSON数组，如：["态度好","发货快"]）',
  `images` VARCHAR(1024) DEFAULT NULL COMMENT '评价图片（JSON数组）',

  `thumbs_up` INT DEFAULT 0 COMMENT '点赞数',
  `thumbs_down` INT DEFAULT 0 COMMENT '点踩数',
  `reported` TINYINT DEFAULT 0 COMMENT '是否被举报（0-否，1-是）',
  `report_reason` VARCHAR(255) DEFAULT NULL COMMENT '举报原因',

  `reply_content` VARCHAR(512) DEFAULT NULL COMMENT '商家回复内容',
  `reply_time` DATETIME DEFAULT NULL COMMENT '商家回复时间',

  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ================================================
-- 初始化测试数据
-- ================================================
-- 插入游戏分类：三角洲行动
INSERT INTO `game_category` (`game_name`, `game_code`, `game_type`, `icon`, `description`, `sort_order`, `status`)
VALUES ('三角洲行动', 'delta_force', 'pc端游', NULL, '三角洲行动（Delta Force）是一款FPS射击游戏', 100, 1);

INSERT INTO `game_category` (`game_name`, `game_code`, `game_type`, `icon`, `description`, `sort_order`, `status`)
VALUES ('三角洲行动手游', 'delta_force_mobile', 'mobile手游', NULL, '三角洲行动手游版', 90, 1);

-- ================================================
-- 说明文档
-- ================================================
-- Sprint 2 新增表: game_category, product, trade_order, order_progress, trade_review
-- 订单状态流转：
-- 待付款(pending_pay) → 已付款(paid) → 进行中(in_progress) → 已提交验收(submitted)
-- → 已确认(confirmed) → 已完成(completed)
--                       ↘ 仲裁中(disputed) ↘ 已取消(cancelled)
