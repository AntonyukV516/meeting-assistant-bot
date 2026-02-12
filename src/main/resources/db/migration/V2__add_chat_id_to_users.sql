-- V2__add_chat_id_to_users.sql
-- Автор: AntonyukV516
-- Дата: 06.02.2026
-- Описание: Добавляем только chat_id для отправки сообщений

ALTER TABLE users
ADD COLUMN IF NOT EXISTS chat_id BIGINT UNIQUE;

-- Индекс для поиска по chat_id
CREATE INDEX IF NOT EXISTS idx_users_chat_id ON users(chat_id);

COMMENT ON COLUMN users.chat_id IS 'ID чата для отправки сообщений';