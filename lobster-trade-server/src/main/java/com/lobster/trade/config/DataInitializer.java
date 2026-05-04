package com.lobster.trade.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        // admin 表：支持权限字段（重建确保结构一致）
        jdbcTemplate.execute("DROP TABLE IF EXISTS admin");
        jdbcTemplate.execute("CREATE TABLE admin (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(64) NOT NULL UNIQUE, " +
                "password VARCHAR(128) NOT NULL, " +
                "nickname VARCHAR(64), " +
                "role VARCHAR(32) DEFAULT 'ADMIN', " +
                "permissions VARCHAR(1024) DEFAULT '*', " +
                "status INT DEFAULT 1, " +
                "last_login_ip VARCHAR(64), " +
                "last_login_time DATETIME, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "is_deleted INT DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 超级管理员拥有全部权限
        jdbcTemplate.execute("INSERT IGNORE INTO admin (username, password, nickname, role, permissions, status) " +
                "VALUES ('admin', 'admin123', '超级管理员', 'SUPER_ADMIN', '*', 1)");

        // 创建操作员账号（无 ADMIN_MANAGE 权限）
        jdbcTemplate.execute("INSERT IGNORE INTO admin (username, password, nickname, role, permissions, status) " +
                "VALUES ('operator', 'operator123', '运营管理员', 'OPERATOR', 'USER_VIEW,USER_EDIT,ORDER_VIEW,ORDER_EDIT,PRODUCT_VIEW,PRODUCT_EDIT,ANNOUNCEMENT_VIEW,ANNOUNCEMENT_EDIT,COUPON_VIEW,COUPON_EDIT,HOTSEARCH_VIEW,HOTSEARCH_EDIT', 1)");

        // admin_role 预置角色表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS admin_role (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "role_code VARCHAR(32) NOT NULL UNIQUE, " +
                "role_name VARCHAR(64) NOT NULL, " +
                "permissions VARCHAR(1024), " +
                "description VARCHAR(256), " +
                "status INT DEFAULT 1, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "is_deleted INT DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 预置角色
        jdbcTemplate.execute("INSERT IGNORE INTO admin_role (role_code, role_name, permissions, description, status) VALUES " +
                "('SUPER_ADMIN', 'SUPER_ADMIN', '*', 'All permissions', 1)");
        jdbcTemplate.execute("INSERT IGNORE INTO admin_role (role_code, role_name, permissions, description, status) VALUES " +
                "('OPERATOR', 'OPERATOR', 'USER_VIEW,USER_EDIT,ORDER_VIEW,ORDER_EDIT,PRODUCT_VIEW,PRODUCT_EDIT,ANNOUNCEMENT_VIEW,ANNOUNCEMENT_EDIT,COUPON_VIEW,COUPON_EDIT,HOTSEARCH_VIEW,HOTSEARCH_EDIT,RECSLOT_VIEW,RECSLOT_EDIT', 'Operator role', 1)");
        jdbcTemplate.execute("INSERT IGNORE INTO admin_role (role_code, role_name, permissions, description, status) VALUES " +
                "('CS_AGENT', 'CS_AGENT', 'CS_VIEW,CS_HANDLE,TICKET_VIEW,TICKET_HANDLE,NOTIFICATION_VIEW,NOTIFICATION_EDIT', 'Customer service role', 1)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `game` (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "game_name VARCHAR(128) NOT NULL, " +
                "game_code VARCHAR(64) NOT NULL UNIQUE, " +
                "game_type VARCHAR(32) DEFAULT 'pc端游', " +
                "description VARCHAR(512), " +
                "sort_order INT DEFAULT 100, " +
                "status INT DEFAULT 1, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "is_deleted INT DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        jdbcTemplate.execute("INSERT IGNORE INTO `game` (game_name, game_code, game_type, description, sort_order, status) " +
                "VALUES ('三角洲行动', 'delta_force', 'pc端游', '腾讯旗舰级FPS游戏', 100, 1)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `hot_search_word` (" +
                "  `id` BIGINT NOT NULL AUTO_INCREMENT," +
                "  `word` VARCHAR(100) NOT NULL," +
                "  `search_count` INT DEFAULT 0," +
                "  `sort_order` INT DEFAULT 100," +
                "  `status` TINYINT DEFAULT 1," +
                "  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  `is_deleted` TINYINT DEFAULT 0," +
                "  PRIMARY KEY (`id`), UNIQUE KEY `uk_word` (`word`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        String[] hotWords = {"三角洲行动","哈夫币","段位代练","游戏币交易","保险箱","烽火模式","战场代练","陪玩服务"};
        for (int i = 0; i < hotWords.length; i++) {
            final int sortOrder = i + 1;
            final String word = hotWords[i];
            jdbcTemplate.execute(String.format(
                "INSERT IGNORE INTO `hot_search_word` (word, search_count, sort_order, status) VALUES ('%s', 0, %d, 1)",
                word, sortOrder));
        }

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `platform_announcement` (" +
                "  `id` BIGINT NOT NULL AUTO_INCREMENT," +
                "  `title` VARCHAR(255) NOT NULL," +
                "  `content` TEXT," +
                "  `type` TINYINT DEFAULT 1," +
                "  `priority` TINYINT DEFAULT 3," +
                "  `status` TINYINT DEFAULT 0," +
                "  `publish_time` DATETIME DEFAULT NULL," +
                "  `end_time` DATETIME DEFAULT NULL," +
                "  `view_count` INT DEFAULT 0," +
                "  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  `is_deleted` TINYINT DEFAULT 0," +
                "  PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        jdbcTemplate.execute("INSERT IGNORE INTO `platform_announcement` (title, content, type, priority, status) " +
                "VALUES ('龙虾道具交易平台上线公告', '欢迎使用龙虾道具交易平台！我们致力于为广大游戏玩家提供安全、便捷的虚拟资产交易服务。平台支持游戏币、代练、装备道具等多种交易类型，全程资金托管保障交易安全。', 1, 2, 1)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_notification` (" +
                "  `id` BIGINT NOT NULL AUTO_INCREMENT," +
                "  `user_id` BIGINT DEFAULT 0," +
                "  `title` VARCHAR(255) NOT NULL," +
                "  `content` TEXT," +
                "  `type` TINYINT DEFAULT 1," +
                "  `level` TINYINT DEFAULT 3," +
                "  `link_url` VARCHAR(512) DEFAULT NULL," +
                "  `status` TINYINT DEFAULT 0," +
                "  `read_time` DATETIME DEFAULT NULL," +
                "  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  `is_deleted` TINYINT DEFAULT 0," +
                "  PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `service_ticket` (" +
                "  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "  `user_id` BIGINT DEFAULT NULL," +
                "  `ticket_no` VARCHAR(32) DEFAULT NULL," +
                "  `subject` VARCHAR(255) NOT NULL," +
                "  `category` TINYINT DEFAULT 5," +
                "  `priority` TINYINT DEFAULT 3," +
                "  `status` TINYINT DEFAULT 1," +
                "  `description` TEXT," +
                "  `images` VARCHAR(2000) DEFAULT NULL," +
                "  `handler_reply` TEXT DEFAULT NULL," +
                "  `handler_id` BIGINT DEFAULT NULL," +
                "  `handler_name` VARCHAR(64) DEFAULT NULL," +
                "  `handle_time` DATETIME DEFAULT NULL," +
                "  `close_time` DATETIME DEFAULT NULL," +
                "  `satisfaction` TINYINT DEFAULT NULL," +
                "  `user_feedback` VARCHAR(500) DEFAULT NULL," +
                "  `last_reply_time` DATETIME DEFAULT NULL," +
                "  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  `is_deleted` TINYINT DEFAULT 0," +
                "  KEY `idx_user_id` (`user_id`)," +
                "  KEY `idx_status` (`status`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 评价表：仅建表，保留历史数据；如字段缺失则 ALTER 补充（向后兼容）
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `trade_review` (" +
                "  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "  `order_id` BIGINT DEFAULT NULL," +
                "  `reviewer_id` BIGINT DEFAULT NULL," +
                "  `reviewed_id` BIGINT DEFAULT NULL," +
                "  `role` TINYINT DEFAULT NULL," +
                "  `rating` TINYINT DEFAULT NULL," +
                "  `content` VARCHAR(500) DEFAULT NULL," +
                "  `is_anonymous` TINYINT DEFAULT 0," +
                "  `is_hidden` TINYINT DEFAULT 0," +
                "  `reply_content` VARCHAR(500) DEFAULT NULL," +
                "  `replier_id` BIGINT DEFAULT NULL," +
                "  `reply_time` DATETIME DEFAULT NULL," +
                "  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  `is_deleted` TINYINT DEFAULT 0," +
                "  KEY `idx_order_id` (`order_id`)," +
                "  KEY `idx_reviewer_id` (`reviewer_id`)," +
                "  KEY `idx_reviewed_id` (`reviewed_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        jdbcTemplate.execute("DROP TABLE IF EXISTS `admin_audit_log`");
        jdbcTemplate.execute("CREATE TABLE `admin_audit_log` (" +
                "  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "  `admin_id` BIGINT DEFAULT NULL," +
                "  `admin_username` VARCHAR(64) DEFAULT NULL," +
                "  `action` VARCHAR(64) DEFAULT NULL," +
                "  `entity_type` VARCHAR(64) DEFAULT NULL," +
                "  `entity_id` BIGINT DEFAULT NULL," +
                "  `detail` VARCHAR(1000) DEFAULT NULL," +
                "  `ip` VARCHAR(64) DEFAULT NULL," +
                "  `user_agent` VARCHAR(512) DEFAULT NULL," +
                "  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  `is_deleted` TINYINT DEFAULT 0," +
                "  KEY `idx_admin_id` (`admin_id`)," +
                "  KEY `idx_action` (`action`)," +
                "  KEY `idx_create_time` (`create_time`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        log.info("DataInitializer: admin & game tables ready");
    }
}