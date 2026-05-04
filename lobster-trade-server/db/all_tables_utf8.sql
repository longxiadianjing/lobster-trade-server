-- Exported from live database at localhost/lobster_trade
-- Tables: ['admin', 'admin_audit_log', 'admin_role', 'admin_user', 'coupon', 'cs_message', 'cs_session', 'game', 'game_category', 'hot_search_word', 'im_message', 'im_session', 'order_progress', 'platform_announcement', 'product', 'product_category', 'product_recommend_slot', 'service_provider_certification', 'service_ticket', 'sms_code', 'sys_notification', 'trade_order', 'trade_review', 'user', 'user_coupon', 'user_login_device', 'user_real_name', 'user_session', 'wallet', 'wallet_transaction']


-- ============================================================
-- Table: admin
-- ============================================================
CREATE TABLE `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password` varchar(128) NOT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `role` varchar(32) DEFAULT 'ADMIN',
  `permissions` varchar(1024) DEFAULT '*',
  `status` int DEFAULT '1',
  `last_login_ip` varchar(64) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: admin_audit_log
-- ============================================================
CREATE TABLE `admin_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT NULL,
  `admin_username` varchar(64) DEFAULT NULL,
  `action` varchar(64) DEFAULT NULL,
  `entity_type` varchar(64) DEFAULT NULL,
  `entity_id` bigint DEFAULT NULL,
  `detail` varchar(1000) DEFAULT NULL,
  `ip` varchar(64) DEFAULT NULL,
  `user_agent` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_action` (`action`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: admin_role
-- ============================================================
CREATE TABLE `admin_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(32) NOT NULL,
  `role_name` varchar(64) NOT NULL,
  `permissions` varchar(1024) DEFAULT NULL,
  `description` varchar(256) DEFAULT NULL,
  `status` int DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=202 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: admin_user
-- ============================================================
CREATE TABLE `admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '绠＄悊鍛業D',
  `username` varchar(64) NOT NULL COMMENT '绠＄悊鍛樼敤鎴峰悕',
  `password` varchar(128) NOT NULL COMMENT '瀵嗙爜',
  `real_name` varchar(64) NOT NULL COMMENT '鐪熷疄濮撳悕',
  `phone` varchar(20) DEFAULT NULL COMMENT '鎵嬫満鍙?',
  `email` varchar(128) DEFAULT NULL COMMENT '閭',
  `role_ids` varchar(128) NOT NULL COMMENT '瑙掕壊ID鍒楄〃锛圝SON鏁扮粍锛?',
  `dept_id` bigint DEFAULT NULL COMMENT '閮ㄩ棬ID',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?侊紙1-姝ｅ父锛?2-绂佺敤锛?',
  `last_login_time` datetime DEFAULT NULL COMMENT '鏈?鍚庣櫥褰曟椂闂?',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '鏈?鍚庣櫥褰旾P',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='绠＄悊鍛樿〃';


-- ============================================================
-- Table: coupon
-- ============================================================
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '优惠券名称',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `type` tinyint NOT NULL COMMENT '1=满减 2=折扣 3=充值赠送',
  `min_amount` decimal(10,2) DEFAULT NULL COMMENT '满减门槛金额',
  `discount_value` decimal(10,2) DEFAULT NULL COMMENT '优惠金额',
  `discount_rate` decimal(5,4) DEFAULT NULL COMMENT '折扣率',
  `recharge_amount` decimal(10,2) DEFAULT NULL COMMENT '充值金额',
  `gift_amount` decimal(10,2) DEFAULT NULL COMMENT '赠送金额',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '发行总量',
  `issued_count` int NOT NULL DEFAULT '0' COMMENT '已发放数量',
  `per_user_limit` int NOT NULL DEFAULT '1' COMMENT '每人限领数量',
  `use_min_amount` decimal(10,2) DEFAULT NULL COMMENT '使用门槛金额',
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '0=禁用 1=启用',
  `scope` tinyint DEFAULT '0' COMMENT '0=全场 1=充值 2=商品购买',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券表';


-- ============================================================
-- Table: cs_message
-- ============================================================
CREATE TABLE `cs_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  `sender_type` tinyint DEFAULT '0',
  `content` text NOT NULL,
  `message_type` varchar(20) DEFAULT 'text',
  `attachment_url` varchar(500) DEFAULT NULL,
  `is_read` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: cs_session
-- ============================================================
CREATE TABLE `cs_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_no` varchar(32) NOT NULL,
  `customer_id` bigint NOT NULL,
  `operator_id` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '0',
  `priority` tinyint DEFAULT '0',
  `handler_name` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `close_time` datetime DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_no` (`session_no`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: game
-- ============================================================
CREATE TABLE `game` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game_name` varchar(128) NOT NULL,
  `game_code` varchar(64) NOT NULL,
  `game_type` varchar(32) DEFAULT 'pc端游',
  `description` varchar(512) DEFAULT NULL,
  `sort_order` int DEFAULT '100',
  `status` int DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `game_code` (`game_code`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: game_category
-- ============================================================
CREATE TABLE `game_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '娓告垙鍒嗙被ID',
  `game_name` varchar(64) NOT NULL COMMENT '娓告垙鍚嶇О锛堝锛氫笁瑙掓床琛屽姩锛?',
  `game_code` varchar(32) NOT NULL COMMENT '娓告垙浠ｇ爜锛堝锛歞elta_force锛?',
  `game_type` varchar(16) NOT NULL COMMENT '娓告垙绫诲瀷锛坧c绔父/mobile鎵嬫父锛?',
  `icon` varchar(255) DEFAULT NULL COMMENT '鍥炬爣URL',
  `description` varchar(255) DEFAULT NULL COMMENT '鎻忚堪',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭锛堣秺澶ц秺闈犲墠锛?',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?侊紙1-涓婄嚎锛?0-涓嬬嚎锛?',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_game_code` (`game_code`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='娓告垙鍒嗙被琛?';


-- ============================================================
-- Table: hot_search_word
-- ============================================================
CREATE TABLE `hot_search_word` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `word` varchar(100) NOT NULL,
  `search_count` int DEFAULT '0',
  `sort_order` int DEFAULT '100',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB AUTO_INCREMENT=697 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: im_message
-- ============================================================
CREATE TABLE `im_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '浼氳瘽ID',
  `sender_id` bigint NOT NULL COMMENT '鍙戦?佽?匢D',
  `sender_role` varchar(10) NOT NULL COMMENT 'buyer/seller/system',
  `message_type` varchar(20) DEFAULT 'text' COMMENT 'text/image/file/system',
  `content` text NOT NULL COMMENT '娑堟伅鍐呭',
  `attachment_url` varchar(500) DEFAULT NULL COMMENT '闄勪欢URL',
  `is_read` tinyint DEFAULT '0' COMMENT '0-鏈 1-宸茶',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: im_session
-- ============================================================
CREATE TABLE `im_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_no` varchar(32) NOT NULL COMMENT '浼氳瘽缂栧彿',
  `order_id` bigint NOT NULL COMMENT '鍏宠仈璁㈠崟ID',
  `buyer_id` bigint NOT NULL COMMENT '涔板ID',
  `seller_id` bigint NOT NULL COMMENT '鍗栧ID',
  `last_message` varchar(255) DEFAULT NULL COMMENT '鏈?鍚庝竴鏉℃秷鎭憳瑕?',
  `last_message_at` datetime DEFAULT NULL COMMENT '鏈?鍚庢秷鎭椂闂?',
  `unread_buyer` int DEFAULT '0' COMMENT '涔板鏈鏁?',
  `unread_seller` int DEFAULT '0' COMMENT '鍗栧鏈鏁?',
  `status` tinyint DEFAULT '1' COMMENT '1-姝ｅ父 2-绂佺敤',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_no` (`session_no`),
  UNIQUE KEY `order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: order_progress
-- ============================================================
CREATE TABLE `order_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '杩涘害ID',
  `order_id` bigint NOT NULL COMMENT '璁㈠崟ID',
  `order_no` varchar(64) NOT NULL COMMENT '璁㈠崟鍙?',
  `progress_percent` int NOT NULL COMMENT '杩涘害鐧惧垎姣旓紙0-100锛?',
  `progress_note` varchar(255) DEFAULT NULL COMMENT '杩涘害璇存槑锛堝锛氬凡瀹屾垚绗?3绔犱换鍔★級',
  `screenshots` varchar(1024) DEFAULT NULL COMMENT '杩涘害鎴浘锛圝SON鏁扮粍锛?',
  `evidence_type` varchar(16) DEFAULT NULL COMMENT '璇佹嵁绫诲瀷锛坰creenshot/video锛?',
  `seller_submit` tinyint DEFAULT '1' COMMENT '鍗栧鏄惁宸叉彁浜わ紙0-鍚︼紝1-鏄級',
  `seller_submit_time` datetime DEFAULT NULL COMMENT '鍗栧鎻愪氦鏃堕棿',
  `buyer_ack` tinyint DEFAULT '0' COMMENT '涔板鏄惁纭锛?0-鍚︼紝1-鏄級',
  `buyer_ack_time` datetime DEFAULT NULL COMMENT '涔板纭鏃堕棿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璁㈠崟杩涘害璁板綍琛?';


-- ============================================================
-- Table: platform_announcement
-- ============================================================
CREATE TABLE `platform_announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `content` text,
  `type` tinyint DEFAULT '1',
  `priority` tinyint DEFAULT '3',
  `status` tinyint DEFAULT '0',
  `publish_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `view_count` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: product
-- ============================================================
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍟嗗搧ID',
  `seller_id` bigint NOT NULL COMMENT '鍗栧鐢ㄦ埛ID',
  `game_id` bigint NOT NULL COMMENT '娓告垙鍒嗙被ID',
  `category_id` bigint DEFAULT NULL COMMENT '鏈嶅姟绫诲瀷鍒嗙被ID锛堝叧鑱攕ervice_category锛?',
  `product_type` varchar(16) NOT NULL COMMENT '鍟嗗搧绫诲瀷锛坆oost/p accompany/escort锛?',
  `title` varchar(128) NOT NULL COMMENT '鍟嗗搧鏍囬',
  `description` text COMMENT '鍟嗗搧璇︽儏鎻忚堪',
  `images` varchar(1024) DEFAULT NULL COMMENT '鍟嗗搧鍥剧墖锛圝SON鏁扮粍锛屾渶澶?9寮狅級',
  `price_type` varchar(16) NOT NULL COMMENT '瀹氫环绫诲瀷锛坒ixed/per_wan/per_hour/per_game锛?',
  `price` decimal(10,2) NOT NULL COMMENT '浠锋牸',
  `unit` varchar(16) DEFAULT NULL COMMENT '鍗曚綅锛堜竾鍝堝か甯?/灏忔椂/灞?锛?',
  `game_zone` varchar(64) DEFAULT NULL COMMENT '娓告垙鍖烘湇锛堝锛氱兘鐏?/鎴樺満锛?',
  `server` varchar(64) DEFAULT NULL COMMENT '鏈嶅姟鍣?',
  `platform` varchar(16) DEFAULT NULL COMMENT '骞冲彴锛圥C绔?/鎵嬫父锛?',
  `min_deposit` decimal(10,2) DEFAULT '0.00' COMMENT '鏈?浣庝繚璇侀噾',
  `estimated_hours` int DEFAULT NULL COMMENT '棰勮瀹屾垚鏃堕暱锛堝皬鏃讹級',
  `stock` int DEFAULT '1' COMMENT '搴撳瓨锛堜唬缁冪被閫氬父涓?1锛?',
  `total_orders` int DEFAULT '0' COMMENT '绱宸插畬鎴愯鍗曟暟',
  `completed_orders` int DEFAULT '0' COMMENT '绱瀹屾垚璁㈠崟鏁?',
  `view_count` int DEFAULT '0' COMMENT '娴忚閲?',
  `favorite_count` int DEFAULT '0' COMMENT '鏀惰棌鏁?',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?侊紙1-涓婃灦锛?2-涓嬫灦锛?3-杩濊灏佺锛?',
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鍟嗗搧琛?';


-- ============================================================
-- Table: product_category
-- ============================================================
CREATE TABLE `product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game_id` bigint NOT NULL,
  `name` varchar(32) NOT NULL,
  `sort_order` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_game_id` (`game_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: product_recommend_slot
-- ============================================================
CREATE TABLE `product_recommend_slot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `slot_key` varchar(64) NOT NULL COMMENT '推荐位标识 home_banner/home_featured/product_detail_side',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sort_order` int DEFAULT '0' COMMENT '排序，越小越靠前',
  `start_time` datetime DEFAULT NULL COMMENT '展示开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '展示结束时间',
  `status` tinyint DEFAULT '1' COMMENT '状态 0=禁用 1=启用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_slot_key` (`slot_key`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品推荐位表';


-- ============================================================
-- Table: service_provider_certification
-- ============================================================
CREATE TABLE `service_provider_certification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `certification_type` varchar(20) NOT NULL COMMENT '璁よ瘉绫诲瀷锛歜oost(浠ｇ粌), accompany(闄帺)',
  `game_id` bigint DEFAULT NULL COMMENT '涓昏娓告垙ID',
  `service_description` varchar(500) DEFAULT NULL COMMENT '鏈嶅姟鎻忚堪',
  `service_regions` varchar(200) DEFAULT NULL COMMENT '鏈嶅姟鍖烘湇锛堝涓敤閫楀彿鍒嗛殧锛?',
  `hourly_rate` decimal(10,2) DEFAULT NULL COMMENT '鍙傝?冩椂浠凤紙鍏?/灏忔椂锛?',
  `credentials` varchar(1000) DEFAULT NULL COMMENT '璧勮川璇佹槑鍥剧墖URLs锛孞SON鏁扮粍',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '鐘舵?侊細0寰呭鏍革紝1閫氳繃锛?2鎷掔粷锛?3鍐荤粨',
  `provider_level` int DEFAULT '1',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '鎷掔粷鍘熷洜',
  `admin_remark` varchar(500) DEFAULT NULL COMMENT '绠＄悊鍛樺娉?',
  `submit_time` datetime DEFAULT NULL COMMENT '鎻愪氦鏃堕棿',
  `review_time` datetime DEFAULT NULL COMMENT '瀹℃牳鏃堕棿',
  `expire_time` datetime DEFAULT NULL COMMENT '璁よ瘉鏈夋晥鏈熸埅姝㈡椂闂?',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: service_ticket
-- ============================================================
CREATE TABLE `service_ticket` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `ticket_no` varchar(32) DEFAULT NULL,
  `subject` varchar(255) NOT NULL,
  `category` tinyint DEFAULT '5',
  `priority` tinyint DEFAULT '3',
  `status` tinyint DEFAULT '1',
  `description` text,
  `images` varchar(2000) DEFAULT NULL,
  `handler_reply` text,
  `handler_id` bigint DEFAULT NULL,
  `handler_name` varchar(64) DEFAULT NULL,
  `handle_time` datetime DEFAULT NULL,
  `close_time` datetime DEFAULT NULL,
  `satisfaction` tinyint DEFAULT NULL,
  `user_feedback` varchar(500) DEFAULT NULL,
  `last_reply_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: sms_code
-- ============================================================
CREATE TABLE `sms_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁板綍ID',
  `phone` varchar(20) NOT NULL COMMENT '鎵嬫満鍙?',
  `code` varchar(8) NOT NULL COMMENT '楠岃瘉鐮?',
  `type` varchar(32) NOT NULL COMMENT '楠岃瘉鐮佺被鍨嬶紙register/login/reset_password锛?',
  `expire_time` datetime NOT NULL COMMENT '杩囨湡鏃堕棿',
  `used` tinyint DEFAULT '0' COMMENT '鏄惁宸蹭娇鐢紙0-鍚︼紝1-鏄級',
  `used_time` datetime DEFAULT NULL COMMENT '浣跨敤鏃堕棿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_phone_type` (`phone`,`type`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐭俊楠岃瘉鐮佽〃';


-- ============================================================
-- Table: sys_notification
-- ============================================================
CREATE TABLE `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT '0',
  `title` varchar(255) NOT NULL,
  `content` text,
  `type` tinyint DEFAULT '1',
  `level` tinyint DEFAULT '3',
  `link_url` varchar(512) DEFAULT NULL,
  `status` tinyint DEFAULT '0',
  `read_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: trade_order
-- ============================================================
CREATE TABLE `trade_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁㈠崟ID',
  `order_no` varchar(64) NOT NULL COMMENT '璁㈠崟鍙?',
  `trade_type` varchar(16) NOT NULL COMMENT '浜ゆ槗绫诲瀷锛坆oost/accompany/escort/goods锛?',
  `product_id` bigint NOT NULL COMMENT '鍟嗗搧ID',
  `product_title` varchar(128) NOT NULL COMMENT '鍟嗗搧鏍囬锛堝啑浣欙級',
  `seller_id` bigint NOT NULL COMMENT '鍗栧ID',
  `buyer_id` bigint NOT NULL COMMENT '涔板ID',
  `game_id` bigint NOT NULL COMMENT '娓告垙ID',
  `category_id` bigint DEFAULT NULL COMMENT '鏈嶅姟鍒嗙被ID',
  `order_amount` decimal(10,2) NOT NULL COMMENT '璁㈠崟鎬讳环',
  `deposit_seller` decimal(10,2) DEFAULT '0.00' COMMENT '鍗栧淇濊瘉閲?',
  `deposit_buyer` decimal(10,2) DEFAULT '0.00' COMMENT '涔板淇濊瘉閲?',
  `commission_rate` decimal(5,4) DEFAULT '0.0500' COMMENT '骞冲彴鎵嬬画璐圭巼锛堥粯璁?0.05锛?',
  `platform_fee` decimal(10,2) DEFAULT '0.00' COMMENT '骞冲彴鎵嬬画璐?',
  `seller_received` decimal(10,2) DEFAULT '0.00' COMMENT '鍗栧瀹炴敹閲戦',
  `escrow_amount` decimal(10,2) NOT NULL COMMENT '鎵樼閲戦',
  `escrow_status` tinyint DEFAULT '1' COMMENT '鎵樼鐘舵?侊紙1-鏈墭绠★紝2-宸叉墭绠★紝3-宸查噴鏀撅紝4-宸查??娆撅級',
  `payment_status` tinyint DEFAULT '0' COMMENT '鏀粯鐘舵?侊紙0-鏈敮浠橈紝1-宸叉敮浠橈紝2-宸查??娆撅級',
  `payment_time` datetime DEFAULT NULL COMMENT '鏀粯鏃堕棿',
  `payment_method` varchar(32) DEFAULT NULL COMMENT '鏀粯鏂瑰紡锛坵allet/alipay/wechat锛?',
  `status` varchar(16) NOT NULL DEFAULT 'pending_pay' COMMENT '璁㈠崟鐘舵??',
  `boost_requirement` text COMMENT '浠ｇ粌瑕佹眰鎻忚堪锛堜拱瀹跺～鍐欑殑鍏蜂綋闇?姹傦級',
  `start_time` datetime DEFAULT NULL COMMENT '浠ｇ粌寮?濮嬫椂闂?',
  `estimated_complete_time` datetime DEFAULT NULL COMMENT '棰勮瀹屾垚鏃堕棿',
  `actual_complete_time` datetime DEFAULT NULL COMMENT '瀹為檯瀹屾垚鏃堕棿',
  `submit_time` datetime DEFAULT NULL COMMENT '鍗栧鎻愪氦楠屾敹鏃堕棿',
  `confirm_time` datetime DEFAULT NULL COMMENT '涔板纭鏃堕棿',
  `delivery_images` varchar(1024) DEFAULT NULL COMMENT '鍙戣揣鍑瘉/鎴浘锛圝SON鏁扮粍锛?',
  `delivery_remark` varchar(255) DEFAULT NULL COMMENT '鍙戣揣澶囨敞',
  `buyer_cancel` tinyint DEFAULT '0' COMMENT '涔板鏄惁鍙栨秷杩囷紙0-鍚︼紝1-鏄級',
  `refund_request` tinyint DEFAULT '0' COMMENT '閫?娆剧敵璇凤紙0-鏃狅紝1-鐢宠涓紝2-宸查??娆撅級',
  `refund_reason` varchar(255) DEFAULT NULL COMMENT '閫?娆惧師鍥?',
  `dispute_status` tinyint DEFAULT '0' COMMENT '浠茶鐘舵?侊紙0-鏃犱徊瑁侊紝1-浠茶涓紝2-浠茶瀹屾垚锛?',
  `dispute_reason` text COMMENT '浠茶鍘熷洜',
  `dispute_result` varchar(255) DEFAULT NULL COMMENT '浠茶缁撴灉',
  `dispute_time` datetime DEFAULT NULL COMMENT '浠茶鏃堕棿',
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璁㈠崟琛?';


-- ============================================================
-- Table: trade_review
-- ============================================================
CREATE TABLE `trade_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `reviewer_id` bigint DEFAULT NULL,
  `reviewed_id` bigint DEFAULT NULL,
  `role` tinyint DEFAULT NULL,
  `rating` tinyint DEFAULT NULL,
  `content` varchar(500) DEFAULT NULL,
  `is_anonymous` tinyint DEFAULT '0',
  `is_hidden` tinyint DEFAULT '0',
  `reply_content` text,
  `replier_id` bigint DEFAULT NULL,
  `reply_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_reviewer_id` (`reviewer_id`),
  KEY `idx_reviewed_id` (`reviewed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ============================================================
-- Table: user
-- ============================================================
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(64) NOT NULL COMMENT '鐢ㄦ埛鍚?',
  `nickname` varchar(64) NOT NULL COMMENT '鏄电О',
  `avatar` varchar(255) DEFAULT NULL COMMENT '澶村儚URL',
  `phone` varchar(20) NOT NULL COMMENT '鎵嬫満鍙?',
  `email` varchar(128) DEFAULT NULL COMMENT '閭',
  `password` varchar(128) NOT NULL COMMENT '瀵嗙爜锛堝姞瀵嗭級',
  `pay_password` varchar(128) DEFAULT NULL COMMENT '鏀粯瀵嗙爜',
  `real_name` varchar(64) DEFAULT NULL COMMENT '鐪熷疄濮撳悕',
  `id_card` varchar(20) DEFAULT NULL COMMENT '韬唤璇佸彿',
  `id_card_front` varchar(255) DEFAULT NULL COMMENT '韬唤璇佹闈?',
  `id_card_back` varchar(255) DEFAULT NULL COMMENT '韬唤璇佸弽闈?',
  `real_name_status` tinyint DEFAULT '0' COMMENT '瀹炲悕鐘舵?侊紙0-鏈疄鍚嶏紝1-宸插疄鍚嶏紝2-瀹℃牳涓紝3-鏈?氳繃锛?',
  `user_level` int DEFAULT '1' COMMENT '鐢ㄦ埛绛夌骇锛?1-鏅?氾紝2-閾滅墝锛?3-閾剁墝锛?4-閲戠墝锛?',
  `reputation_score` decimal(3,2) DEFAULT '5.00' COMMENT '淇¤獕璇勫垎锛?1-5锛?',
  `total_trade_count` int DEFAULT '0' COMMENT '绱浜ゆ槗娆℃暟',
  `total_trade_amount` decimal(12,2) DEFAULT '0.00' COMMENT '绱浜ゆ槗閲戦',
  `balance` decimal(12,2) DEFAULT '0.00' COMMENT '閽卞寘浣欓锛堝厓锛?',
  `frozen_balance` decimal(12,2) DEFAULT '0.00' COMMENT '鍐荤粨閲戦锛堝厓锛?',
  `status` tinyint DEFAULT '1' COMMENT '璐﹀彿鐘舵?侊紙1-姝ｅ父锛?2-灏佺锛?3-鍐荤粨锛?',
  `last_login_time` datetime DEFAULT NULL COMMENT '鏈?鍚庣櫥褰曟椂闂?',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '鏈?鍚庣櫥褰旾P',
  `register_ip` varchar(64) DEFAULT NULL COMMENT '娉ㄥ唽IP',
  `register_source` varchar(32) DEFAULT NULL COMMENT '娉ㄥ唽鏉ユ簮锛圥C/H5/APP锛?',
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛琛?';


-- ============================================================
-- Table: user_coupon
-- ============================================================
CREATE TABLE `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `receive_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `use_time` datetime DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0=未使用 1=已使用 2=已过期',
  `coupon_name` varchar(128) DEFAULT NULL,
  `discount_value` decimal(10,2) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券表';


-- ============================================================
-- Table: user_login_device
-- ============================================================
CREATE TABLE `user_login_device` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_fingerprint` varchar(256) DEFAULT NULL,
  `device_type` varchar(32) DEFAULT NULL COMMENT 'PC/iOS/Android',
  `device_name` varchar(128) DEFAULT NULL,
  `ip_address` varchar(64) DEFAULT NULL,
  `login_location` varchar(128) DEFAULT NULL,
  `os_version` varchar(64) DEFAULT NULL,
  `browser_version` varchar(64) DEFAULT NULL,
  `is_current` tinyint DEFAULT '0',
  `is_trusted` tinyint DEFAULT '0',
  `last_active_time` datetime DEFAULT NULL,
  `first_login_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_fingerprint` (`device_fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录设备表';


-- ============================================================
-- Table: user_real_name
-- ============================================================
CREATE TABLE `user_real_name` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `real_name` varchar(64) NOT NULL,
  `id_card` varchar(20) NOT NULL,
  `id_card_front` varchar(255) DEFAULT NULL,
  `id_card_back` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0=审核中 1=已通过 2=未通过 3=已撤销',
  `reject_reason` varchar(255) DEFAULT NULL,
  `aliyun_verify_token` varchar(256) DEFAULT NULL,
  `aliyun_verify_result` text,
  `verify_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户实名表';


-- ============================================================
-- Table: user_session
-- ============================================================
CREATE TABLE `user_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浼氳瘽ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `token` varchar(512) NOT NULL COMMENT 'Token',
  `device_info` varchar(255) DEFAULT NULL COMMENT '璁惧淇℃伅',
  `ip` varchar(64) DEFAULT NULL COMMENT '鐧诲綍IP',
  `expire_time` datetime NOT NULL COMMENT '杩囨湡鏃堕棿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_token` (`token`(255)),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛浼氳瘽琛?';


-- ============================================================
-- Table: wallet
-- ============================================================
CREATE TABLE `wallet` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '閽卞寘ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '浣欓锛堝厓锛?',
  `frozen_balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '鍐荤粨閲戦锛堝厓锛?',
  `total_income` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '绱鏀跺叆',
  `total_expense` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '绱鏀嚭',
  `password_set` tinyint DEFAULT '0' COMMENT '鏄惁璁剧疆鏀粯瀵嗙爜锛?0-鍚︼紝1-鏄級',
  `password` varchar(128) DEFAULT NULL COMMENT '鏀粯瀵嗙爜锛堝姞瀵嗭級',
  `alipay_account` varchar(64) DEFAULT NULL COMMENT '鏀粯瀹濊处鍙?',
  `alipay_name` varchar(64) DEFAULT NULL COMMENT '鏀粯瀹濆疄鍚?',
  `wechat_openid` varchar(64) DEFAULT NULL COMMENT '寰俊OpenID',
  `bank_card_no` varchar(32) DEFAULT NULL COMMENT '閾惰鍗″彿',
  `bank_name` varchar(64) DEFAULT NULL COMMENT '寮?鎴疯',
  `bank_username` varchar(64) DEFAULT NULL COMMENT '閾惰鍗″疄鍚?',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='閽卞寘琛?';


-- ============================================================
-- Table: wallet_transaction
-- ============================================================
CREATE TABLE `wallet_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '娴佹按ID',
  `trans_no` varchar(64) NOT NULL COMMENT '娴佹按鍙?',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `type` tinyint NOT NULL COMMENT '绫诲瀷锛?1-鏀跺叆锛?2-鏀嚭锛?3-鍐荤粨锛?4-瑙ｅ喕锛?5-閫?娆撅級',
  `amount` decimal(12,2) NOT NULL COMMENT '鍙樺姩閲戦锛堝厓锛?',
  `balance_before` decimal(12,2) NOT NULL COMMENT '鍙樺姩鍓嶄綑棰?',
  `balance_after` decimal(12,2) NOT NULL COMMENT '鍙樺姩鍚庝綑棰?',
  `frozen_before` decimal(12,2) DEFAULT '0.00' COMMENT '鍙樺姩鍓嶅喕缁撻噾棰?',
  `frozen_after` decimal(12,2) DEFAULT '0.00' COMMENT '鍙樺姩鍚庡喕缁撻噾棰?',
  `source` varchar(32) NOT NULL COMMENT '鏉ユ簮锛坥rder/recharge/withdraw/commission/refund锛?',
  `source_id` bigint DEFAULT NULL COMMENT '鏉ユ簮ID锛堣鍗旾D/鍏呭?糏D绛夛級',
  `source_no` varchar(64) DEFAULT NULL COMMENT '鏉ユ簮鍗曞彿',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '鐘舵?侊紙1-鎴愬姛锛?2-澶辫触锛?3-澶勭悊涓級',
  `remark` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trans_no` (`trans_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_source` (`source`,`source_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='閽卞寘娴佹按琛?';

