# Meeting Assistant Bot 🤖

Telegram-бот для организации встреч с друзьями и коллегами.

## 📋 Команды

| Команда        | Кнопка             | Описание                  |
|----------------|--------------------|---------------------------|
| `/start`       | –                  | Начать работу с ботом     |
| `/new`         | 📝 Создать встречу | Создать новую встречу     |
| `/my_meetings` | 📋 Мои встречи     | Список моих встреч        |
| `/cancel`      | ❌ Отмена           | Отменить текущее действие |
| `/help`        | ❓ Помощь           | Показать справку          |

## 🚀 Быстрый старт

```bash
# 1. Клонировать
git clone https://github.com/AntonyukV516/meeting-assistant-bot.git
cd meeting-assistant-bot

# 2. Создать .env файл
echo "BOT_TOKEN=ваш_токен_от_BotFather" > .env
echo "BOT_USERNAME=username_бота" >> .env

# 3. Запустить
docker-compose up --build
```

### После запуска:

Напишите боту /start в Telegram

Health check: http://localhost:8080/actuator/health

Prometheus: http://localhost:9090

Grafana: http://localhost:3000 (admin/admin123)

## 🛠 Технологии

* Java 21 — основной язык разработки

* Spring Boot 3.5.9 — фреймворк для приложения

* PostgreSQL 16 + Flyway — база данных и миграции

* Telegram Bots API — библиотека для работы с Telegram

* Docker + Docker Compose — контейнеризация

* Prometheus + Grafana — мониторинг и метрики

* JUnit 5 + Mockito — тестирование

## 📊 Мониторинг

| Сервис         | URL                                   | Логин/пароль   |
|----------------|---------------------------------------|----------------|
| `Health Check` | http://localhost:8080/actuator/health | -              |
| `Prometheus`   | http://localhost:9090                 | -              |
| `Grafana`      | http://localhost:3000                 | admin/admin123 |


## 📝Лицензия

© 2026 [AntonyukV516](https://github.com/AntonyukV516). Все права защищены.

Данный программный код является интеллектуальной собственностью автора
и предоставляется исключительно для ознакомления.
Любое использование, копирование, модификация или распространение 
данного программного обеспечения без письменного разрешения автора запрещено.

## 📬 Контакты
Автор: AntonyukV516
* GitHub: @AntonyukV516
* Telegram: @Antonyuk516
* E-mail: antonyukV516@yandex.ru