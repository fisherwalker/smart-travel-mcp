-- ============================================
-- 数据隔离迁移脚本 v2
-- 为 chat_session 和 chat_message 添加 user_id 实现用户数据分档
-- 执行方式: mysql -u root -p smart_travel < sql/migrate_v2_add_user_id.sql
-- ============================================

USE smart_travel;

-- 1. chat_session 添加 user_id 列
-- 先加 DEFAULT 0 避免 NOT NULL 报错（如果表中有数据），然后移除 DEFAULT
ALTER TABLE chat_session
    ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属用户ID' AFTER id;

ALTER TABLE chat_session
    ALTER COLUMN user_id DROP DEFAULT;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_session_user ON chat_session (user_id);

-- 2. chat_message 添加 user_id 列
ALTER TABLE chat_message
    ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属用户ID' AFTER id;

ALTER TABLE chat_message
    ALTER COLUMN user_id DROP DEFAULT;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_msg_user ON chat_message (user_id);

-- 验证
SELECT 'chat_session columns:' AS '';
DESC chat_session;
SELECT 'chat_message columns:' AS '';
DESC chat_message;
SELECT '✅ 迁移完成！user_id 列已添加' AS '';
