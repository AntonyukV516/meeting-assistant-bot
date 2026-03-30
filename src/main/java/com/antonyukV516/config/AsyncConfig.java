package com.antonyukV516.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфигурация асинхронного выполнения задач.
 * <p>
 * Настраивает пулы потоков для фоновых операций:
 * <ul>
 *   <li>Рассылка уведомлений пользователям</li>
 *   <li>Фоновая обработка тяжелых задач</li>
 * </ul>
 * </p>
 *
 * @author AntonyukV516
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Пул потоков для отправки уведомлений.
     * <p>
     * Характеристики:
     * <ul>
     *   <li>core-pool-size: 5 — всегда готовы 5 потоков</li>
     *   <li>max-pool-size: 20 — до 20 при нагрузке</li>
     *   <li>queue-capacity: 100 — очередь задач</li>
     *   <li>thread-name-prefix: "notification-" — для логов</li>
     * </ul>
     * </p>
     *
     * @return Executor для уведомлений
     */
    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }
}