-- 热搜词表
CREATE TABLE IF NOT EXISTS `hot_search_word` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `word` VARCHAR(100) NOT NULL COMMENT '热搜词',
  `search_count` INT DEFAULT 0 COMMENT '搜索次数',
  `sort_order` INT DEFAULT 100 COMMENT '排序顺序（越小越靠前）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`),
  KEY `idx_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜词表';

-- 初始数据
INSERT INTO `hot_search_word` (`word`, `search_count`, `sort_order`, `status`) VALUES
('三角洲行动', 0, 1, 1),
('哈夫币', 0, 2, 1),
('段位代练', 0, 3, 1),
('游戏币交易', 0, 4, 1),
('保险箱', 0, 5, 1),
('烽火模式', 0, 6, 1),
('战场代练', 0, 7, 1),
('陪玩服务', 0, 8, 1);
