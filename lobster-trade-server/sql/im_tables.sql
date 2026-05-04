-- IM 即时通讯：会话表 + 消息表
-- 会话：每个订单一个会话，买卖家双方参与
CREATE TABLE IF NOT EXISTS im_session (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_no      VARCHAR(32)  NOT NULL UNIQUE COMMENT '会话编号',
    order_id        BIGINT       NOT NULL UNIQUE COMMENT '关联订单ID',
    buyer_id        BIGINT       NOT NULL COMMENT '买家ID',
    seller_id       BIGINT       NOT NULL COMMENT '卖家ID',
    last_message    VARCHAR(255) DEFAULT NULL COMMENT '最后一条消息摘要',
    last_message_at DATETIME     DEFAULT NULL COMMENT '最后消息时间',
    unread_buyer    INT          DEFAULT 0 COMMENT '买家未读数',
    unread_seller   INT          DEFAULT 0 COMMENT '卖家未读数',
    status          TINYINT      DEFAULT 1 COMMENT '1-正常 2-禁用',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消息表
CREATE TABLE IF NOT EXISTS im_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id      BIGINT       NOT NULL COMMENT '会话ID',
    sender_id       BIGINT       NOT NULL COMMENT '发送者ID',
    sender_role     VARCHAR(10)  NOT NULL COMMENT 'buyer/seller/system',
    message_type    VARCHAR(20)  DEFAULT 'text' COMMENT 'text/image/file/system',
    content         TEXT         NOT NULL COMMENT '消息内容',
    attachment_url  VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    is_read         TINYINT      DEFAULT 0 COMMENT '0-未读 1-已读',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    is_deleted      TINYINT      DEFAULT 0,
    INDEX idx_session_id (session_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
