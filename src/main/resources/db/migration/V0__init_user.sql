-- Автор: AntonyukV516
-- Дата: 20.01.2026
-- Описание: Создание пользователя приложения

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'bot_user') THEN
        CREATE USER bot_user WITH PASSWORD 'bot_password';
    END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE meeting_bot_db TO bot_user;