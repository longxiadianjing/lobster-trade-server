-- ============================================================
-- 龙虾道具交易平台 - seed_data.sql
-- 基础数据初始化脚本
-- 包含：游戏分类、测试用户、管理员角色、示例商品等
-- ============================================================

USE `lobster_trade`;

-- ============================================================
-- 1. 管理员角色
-- ============================================================
INSERT INTO `admin_role` (`role_code`, `role_name`, `permissions`, `description`, `status`) VALUES
('SUPER_ADMIN', '超级管理员', '*', '拥有所有权限', 1),
('OPERATOR', '运营管理员', 'USER_VIEW,USER_EDIT,ORDER_VIEW,ORDER_EDIT,PRODUCT_VIEW,PRODUCT_EDIT,ANNOUNCEMENT_VIEW,ANNOUNCEMENT_EDIT,COUPON_VIEW,COUPON_EDIT,HOTSEARCH_VIEW,HOTSEARCH_EDIT,RECSLOT_VIEW,RECSLOT_EDIT', '运营管理角色', 1),
('CS_AGENT', '客服', 'CS_VIEW,CS_HANDLE,TICKET_VIEW,TICKET_HANDLE,NOTIFICATION_VIEW,NOTIFICATION_EDIT', '客服角色', 1);

-- ============================================================
-- 2. 管理员账号（密码均为 admin123，BCrypt加密）
-- ============================================================
INSERT INTO `admin` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 'SUPER_ADMIN', 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '运营管理员', 'OPERATOR', 1);

INSERT INTO `admin_user` (`username`, `password`, `real_name`, `role_ids`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', '[1]', 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '运营管理员', '[2]', 1);

-- ============================================================
-- 3. 游戏分类
-- ============================================================
INSERT INTO `game_category` (`game_name`, `game_code`, `game_type`, `icon`, `description`, `sort_order`, `status`) VALUES
('三角洲行动', 'delta_force', 'fps', 'delta.png', '腾讯战术射击游戏', 1, 1),
('王者荣耀', 'wangzhe', 'moba', 'wangzhe.png', '5V5 MOBA竞技游戏', 2, 1),
('英雄联盟', 'lol', 'moba', 'lol.png', '经典MOBA游戏', 3, 1),
('和平精英', 'peace_elite', 'fps', 'peace.png', '腾讯战术竞技手游', 4, 1),
('穿越火线', 'cf', 'fps', 'cf.png', '第一人称射击游戏', 5, 1),
('无畏契约', 'valorant', 'fps', 'valorant.png', '拳头公司战术射击游戏', 6, 1),
('DNF', 'dnf', 'mmo', 'dnf.png', '地下城与勇士', 7, 1),
('剑网3', 'jx3', 'mmo', 'jx3.png', '剑侠情缘网络版', 8, 1);

-- ============================================================
-- 4. 商品服务分类
-- ============================================================
-- 三角洲行动 (game_id=21)
INSERT INTO `product_category` (`game_id`, `name`, `sort_order`, `status`) VALUES
(21, '游戏币', 1, 1),
(21, '装备', 2, 1),
(21, '代练', 3, 1),
(21, '账号', 4, 1),
(21, '陪玩', 5, 1),
(21, '皮肤', 6, 1),
-- 王者荣耀 (game_id=22)
(22, '游戏币', 1, 1),
(22, '英雄', 2, 1),
(22, '代练', 3, 1),
(22, '陪玩', 4, 1),
-- 英雄联盟 (game_id=23)
(23, '游戏币', 1, 1),
(23, '代练', 2, 1),
(23, '账号', 3, 1),
-- 和平精英 (game_id=24)
(24, '游戏币', 1, 1),
(24, '装备', 2, 1),
(24, '代练', 3, 1),
(24, '皮肤', 4, 1);

-- ============================================================
-- 5. 测试用户（密码均为 Test123456，BCrypt加密）
--     BCrypt("Test123456") = $2a$10$...
-- ============================================================
INSERT INTO `user` (`username`, `nickname`, `phone`, `password`, `balance`, `status`, `real_name_status`, `user_level`, `reputation_score`) VALUES
-- 买家账号
('13812340001', '测试买家小明', '13812340001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1000.00, 1, 1, 2, 5.00),
-- 卖家账号
('13900002222', '专业代练小王', '13900002222', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 500.00, 1, 1, 3, 5.00),
('13900002223', '资深陪玩小李', '13900002223', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 800.00, 1, 1, 3, 4.80),
-- 普通测试用户
('testbuyer', '测试买家', '13800001001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 2000.00, 1, 0, 1, 5.00),
('testseller', '测试卖家', '13800001002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 500.00, 1, 0, 2, 4.50);

-- ============================================================
-- 6. 钱包初始化（对应上面的测试用户）
-- ============================================================
INSERT INTO `wallet` (`user_id`, `balance`, `frozen_balance`, `total_income`, `total_expense`, `password_set`, `password`) VALUES
(1, 1000.00, 0.00, 0.00, 0.00, 1, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH'),
(2, 500.00, 0.00, 0.00, 0.00, 1, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH'),
(3, 800.00, 0.00, 0.00, 0.00, 0, NULL),
(4, 2000.00, 0.00, 0.00, 0.00, 0, NULL),
(5, 500.00, 0.00, 0.00, 0.00, 0, NULL);

-- ============================================================
-- 7. 热搜词
-- ============================================================
INSERT INTO `hot_search_word` (`word`, `search_count`, `sort_order`, `status`) VALUES
('三角洲行动', 0, 1, 1),
('哈夫币', 0, 2, 1),
('段位代练', 0, 3, 1),
('游戏币交易', 0, 4, 1),
('保险箱', 0, 5, 1),
('烽火模式', 0, 6, 1),
('战场代练', 0, 7, 1),
('陪玩服务', 0, 8, 1),
('王者荣耀代练', 0, 9, 1),
('和平精英灵敏度', 0, 10, 1);

-- ============================================================
-- 8. 优惠券
-- ============================================================
INSERT INTO `coupon` (`name`, `description`, `type`, `min_amount`, `discount_value`, `total_count`, `issued_count`, `per_user_limit`, `use_min_amount`, `status`, `scope`) VALUES
('新用户10元券', '新注册用户首单立减10元', 1, 50.00, 10.00, 10000, 0, 1, 50.00, 1, 2),
('满100减20', '充值满100元立减20元', 1, 100.00, 20.00, 5000, 0, 3, 100.00, 1, 1),
('95折优惠券', '全场95折优惠', 2, NULL, NULL, 0.9500, 2000, 0, 1, 1, 0, 0),
('充值100送10', '充值100元赠送10元', 3, 100.00, NULL, NULL, 10.00, 10000, 0, 2, 100.00, 1, 1);

-- ============================================================
-- 9. 平台公告
-- ============================================================
INSERT INTO `platform_announcement` (`title`, `content`, `type`, `priority`, `status`, `publish_time`) VALUES
('欢迎使用龙虾道具交易平台', '龙虾道具交易平台正式上线！提供安全的游戏道具交易服务，支持代练、陪玩、装备交易等多种交易类型。', 1, 1, 1, NOW()),
('平台交易手续费调整公告', '即日起，平台交易手续费调整为5%，具体详见规则说明。', 1, 2, 1, NOW()),
('实名认证奖励通知', '完成实名认证的用户可获得专属优惠券，欢迎大家积极参与！', 2, 3, 1, NOW());

-- ============================================================
-- 10. 示例商品数据
-- ============================================================
INSERT INTO `product` (`seller_id`, `game_id`, `category_id`, `product_type`, `title`, `description`, `images`, `price_type`, `price`, `unit`, `game_zone`, `platform`, `stock`, `status`) VALUES
-- 三角洲行动游戏币
(2, 21, 1, 'game_currency', '三角洲行动 哈夫币 100万', '官方渠道，安全可靠，秒到账，支持烽火/战场模式', NULL, 'per_wan', 80.00, '万哈夫币', '烽火/战场', 'PC端', 99, 1),
(2, 21, 3, 'boosting', '三角洲行动 战场代练 段位提升', '专业代练，段位提升服务，从新兵到上尉，诚信经营', NULL, 'fixed', 200.00, NULL, '战场', 'PC端', 1, 1),
(3, 21, 5, 'accompany', '三角洲行动 烽火模式 美女陪玩', '技术陪玩，聊天陪伴，包上分，语音通话', NULL, 'per_hour', 50.00, '小时', '烽火', 'PC端', 10, 1),
-- 王者荣耀游戏币
(2, 22, 1, 'game_currency', '王者荣耀 荣耀水晶 5000个', '荣耀水晶，可用于积分夺宝，保底出货', NULL, 'fixed', 350.00, NULL, '微信/QQ', '手游', 50, 1),
-- 和平精英装备
(3, 24, 2, 'equipment', '和平精英 稀有皮肤套装', '和平精英限定皮肤套装，炫酷稀有，支持扫码交易', NULL, 'fixed', 500.00, NULL, '全服', '手游', 5, 1);
