-- ============================================================
-- 短链接系统 · 阶段1 初始化脚本
-- 数据库：shortlink（MySQL 8.0+）
-- 说明：
--   1. 旧表 short_link 属于早期实验，按用户要求删除；
--   2. t_short_link 为阶段1正式表结构。
-- ============================================================

CREATE DATABASE IF NOT EXISTS shortlink
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE shortlink;

-- 删除早期实验表（已确认不再使用）
DROP TABLE IF EXISTS `short_link`;

-- 删除旧的正式表，保证脚本可重复执行
DROP TABLE IF EXISTS `t_short_link`;

CREATE TABLE `t_short_link` (
  `id`          BIGINT        NOT NULL COMMENT '雪花ID',
  `short_code`  VARCHAR(10)   NOT NULL COMMENT '短码',
  `long_url`    VARCHAR(2048) NOT NULL COMMENT '长链接',
  `user_id`     BIGINT                 COMMENT '创建人',
  `visit_count` BIGINT        NOT NULL DEFAULT 0 COMMENT '访问次数',
  `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `expire_time` DATETIME               COMMENT '过期时间，NULL永不过期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_short_code` (`short_code`),
  KEY `idx_user` (`user_id`),
  KEY `idx_long_url` (`long_url`(191))
) ENGINE=InnoDB COMMENT='短链接表';
