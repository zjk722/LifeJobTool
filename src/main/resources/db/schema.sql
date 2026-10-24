-- 家政派单系统核心表结构（MySQL 8）
-- 手动执行：mysql -uroot -p < schema.sql
-- 说明：完整表清单见仓库外《家政派单系统-需求与设计.md》，这里只放骨架阶段要用的核心表。

CREATE DATABASE IF NOT EXISTS home_service
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE home_service;

CREATE TABLE IF NOT EXISTS worker (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name           VARCHAR(32)     NOT NULL,
    phone          VARCHAR(20)     NOT NULL,
    status         VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/NORMAL/FROZEN',
    credit_score   INT             NOT NULL DEFAULT 100,
    rating         DECIMAL(3, 2)   NOT NULL DEFAULT 5.00,
    order_count    INT             NOT NULL DEFAULT 0,
    service_radius INT             NOT NULL DEFAULT 5000 COMMENT '服务半径，单位米',
    home_lat       DECIMAL(10, 7)  NULL,
    home_lng       DECIMAL(10, 7)  NULL,
    created_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_worker_phone (phone),
    KEY idx_worker_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS service_item (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id  BIGINT UNSIGNED NOT NULL,
    name         VARCHAR(64)     NOT NULL,
    duration_min INT             NOT NULL COMMENT '预计服务时长，分钟',
    base_price   DECIMAL(10, 2)  NOT NULL,
    status       TINYINT         NOT NULL DEFAULT 1 COMMENT '1 上架 0 下架',
    created_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_item_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS service_order (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_no          VARCHAR(32)     NOT NULL,
    user_id           BIGINT UNSIGNED NOT NULL,
    worker_id         BIGINT UNSIGNED NULL,
    address_id        BIGINT UNSIGNED NOT NULL,
    appointment_start DATETIME        NOT NULL,
    appointment_end   DATETIME        NOT NULL,
    status            VARCHAR(24)     NOT NULL,
    dispatch_mode     VARCHAR(16)     NULL COMMENT 'AUTO/GRAB/MANUAL',
    total_amount      DECIMAL(10, 2)  NOT NULL DEFAULT 0,
    discount_amount   DECIMAL(10, 2)  NOT NULL DEFAULT 0,
    pay_amount        DECIMAL(10, 2)  NOT NULL DEFAULT 0,
    commission_rate   DECIMAL(5, 4)   NOT NULL DEFAULT 0.2000,
    cancel_reason     VARCHAR(255)    NULL,
    remark            VARCHAR(255)    NULL,
    version           INT             NOT NULL DEFAULT 0,
    created_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    paid_at           DATETIME(3)     NULL,
    assigned_at       DATETIME(3)     NULL,
    finished_at       DATETIME(3)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user_status (user_id, status),
    KEY idx_order_worker_start (worker_id, appointment_start),
    KEY idx_order_status_start (status, appointment_start)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS worker_slot (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    worker_id  BIGINT UNSIGNED NOT NULL,
    slot_start DATETIME        NOT NULL,
    slot_end   DATETIME        NOT NULL,
    status     VARCHAR(16)     NOT NULL DEFAULT 'FREE' COMMENT 'FREE/LOCKED/BOOKED',
    order_id   BIGINT UNSIGNED NULL,
    version    INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_worker_slot (worker_id, slot_start),
    KEY idx_slot_order (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS dispatch_task (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id   BIGINT UNSIGNED NOT NULL,
    worker_id  BIGINT UNSIGNED NOT NULL,
    score      DECIMAL(8, 2)    NOT NULL DEFAULT 0,
    status     VARCHAR(16)     NOT NULL COMMENT 'OFFERED/ACCEPTED/REJECTED/TIMEOUT',
    expire_at  DATETIME(3)     NOT NULL,
    created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_order_worker (order_id, worker_id),
    KEY idx_task_worker_status (worker_id, status),
    KEY idx_task_status_expire (status, expire_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS payment (
    id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    pay_no   VARCHAR(40)     NOT NULL,
    channel  VARCHAR(16)     NOT NULL COMMENT 'MOCK/ALIPAY/WECHAT',
    amount   DECIMAL(10, 2)  NOT NULL,
    status   VARCHAR(16)     NOT NULL COMMENT 'INIT/SUCCESS/FAILED/REFUNDED',
    paid_at  DATETIME(3)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_pay_no (pay_no),
    UNIQUE KEY uk_payment_order (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS settlement_detail (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    settlement_id BIGINT UNSIGNED NULL,
    order_id      BIGINT UNSIGNED NOT NULL,
    worker_id     BIGINT UNSIGNED NOT NULL,
    gross_amount  DECIMAL(10, 2)  NOT NULL,
    commission    DECIMAL(10, 2)  NOT NULL,
    net_amount    DECIMAL(10, 2)  NOT NULL,
    status        VARCHAR(16)     NOT NULL COMMENT 'PENDING/SETTLED',
    created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_settlement_order (order_id),
    KEY idx_settlement_worker_status (worker_id, status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
