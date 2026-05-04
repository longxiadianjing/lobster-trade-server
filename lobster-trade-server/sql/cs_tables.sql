-- ============================================================
-- 客服系统：会话表 + 消息表
-- ============================================================

CREATE TABLE IF NOT EXISTS cs_session (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_no      VARCHAR(32)  NOT NULL UNIQUE COMMENT '会话编号',
    customer_id     BIGINT       NOT NULL COMMENT '客户ID（用户）',
    operator_id     BIGINT       DEFAULT NULL COMMENT '客服人员ID',
    order_id        BIGINT       DEFAULT NULL COMMENT '关联订单ID（可选）',
    subject         VARCHAR(255) DEFAULT NULL COMMENT '会话主题',
    status          TINYINT      DEFAULT 0 COMMENT '0=等待中 1=进行中 2=已关闭',
    priority        TINYINT      DEFAULT 0 COMMENT '0=普通 1=紧急',
    handler_name    VARCHAR(64)  DEFAULT NULL COMMENT '处理人姓名',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    close_time      DATETIME     DEFAULT NULL,
    is_deleted      TINYINT      DEFAULT 0,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cs_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id      BIGINT       NOT NULL COMMENT '会话ID',
    sender_id       BIGINT       NOT NULL COMMENT '发送者ID',
    sender_type     TINYINT      DEFAULT 0 COMMENT '0=用户 1=客服',
    content         TEXT         NOT NULL COMMENT '消息内容',
    message_type    VARCHAR(20)  DEFAULT 'text' COMMENT 'text/image/file',
    attachment_url  VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    is_read         TINYINT      DEFAULT 0 COMMENT '0=未读 1=已读',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    is_deleted      TINYINT      DEFAULT 0,
    INDEX idx_session_id (session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
