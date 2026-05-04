-- 服务商认证表（代练/陪玩服务商认证）
CREATE TABLE IF NOT EXISTS `service_provider_certification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `certification_type` VARCHAR(20) NOT NULL COMMENT '认证类型：boost(代练), accompany(陪玩)',
    `game_id` BIGINT COMMENT '主要游戏ID',
    `service_description` VARCHAR(500) COMMENT '服务描述',
    `service_regions` VARCHAR(200) COMMENT '服务区服（多个用逗号分隔）',
    `hourly_rate` DECIMAL(10,2) COMMENT '参考时价（元/小时）',
    `credentials` VARCHAR(1000) COMMENT '资质证明图片URLs，JSON数组',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待审核，1通过，2拒绝，3冻结',
    `reject_reason` VARCHAR(500) COMMENT '拒绝原因',
    `admin_remark` VARCHAR(500) COMMENT '管理员备注',
    `submit_time` DATETIME COMMENT '提交时间',
    `review_time` DATETIME COMMENT '审核时间',
    `expire_time` DATETIME COMMENT '认证有效期截止时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户表新增认证相关字段（如果还没有）
--ALTER TABLE `user` ADD COLUMN `is_certified_provider` TINYINT DEFAULT 0 COMMENT '是否认证服务商';
--ALTER TABLE `user` ADD COLUMN `provider_cert_type` VARCHAR(20) COMMENT '服务商认证类型';
