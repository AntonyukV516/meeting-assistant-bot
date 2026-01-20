-- V1__create_tables.sql
-- Автор: AntonyukV516
-- Дата: 19.01.2026
-- Описание: Создание базовых таблиц для Meeting Assistant Bot

-- Включение расширения для UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Таблица пользователей Telegram
CREATE TABLE users (
    telegram_username VARCHAR(50) PRIMARY KEY
);

-- Таблица встреч
CREATE TABLE meetings (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description TEXT,
    date_time TIMESTAMP,
    location VARCHAR(100),
    max_people INTEGER,
    creator_username VARCHAR(50) NOT NULL REFERENCES users(telegram_username) ON DELETE CASCADE
);

-- Таблица тегов встреч (Enum Tag будет храниться как VARCHAR)
CREATE TABLE meeting_tags (
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (meeting_id, tag)
);

-- Индексы для улучшения производительности
CREATE INDEX idx_meetings_creator ON meetings(creator_username);
CREATE INDEX idx_meeting_tags_meeting_id ON meeting_tags(meeting_id);
CREATE INDEX idx_meeting_tags_tag ON meeting_tags(tag);

COMMENT ON TABLE users IS 'Пользователи Telegram, которые запустили бота';
COMMENT ON TABLE meetings IS 'Встречи, созданные пользователями';
COMMENT ON TABLE meeting_tags IS 'Теги встреч';