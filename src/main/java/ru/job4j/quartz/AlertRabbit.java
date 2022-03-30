package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 * 1. Конфигурирование.
 * Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
 * scheduler.start();
 * Начало работы происходит с создания класса управляющего всеми работами.
 * В объект Scheduler мы будем добавлять задачи, которые хотим выполнять периодически.
 * <p>
 * 2. Создание задачи.
 * JobDetail job = newJob(Rabbit.class).build()
 * quartz каждый раз создает объект с типом org.quartz.Job. Вам нужно создать класс реализующий этот интерфейс.
 * Внутри этого класса (class Rabbit implements Job) нужно описать требуемые действия.
 * В нашем случае - это вывод на консоль текста.
 * <p>
 * 3. Создание расписания.
 * SimpleScheduleBuilder times = simpleSchedule()
 * .withIntervalInSeconds(10)
 * .repeatForever();
 * Конструкция выше настраивает периодичность запуска.
 * В нашем случае, мы будем запускать задачу через 10 секунд и делать это бесконечно.
 * <p>
 * 4. Задача выполняется через триггер.
 * Trigger trigger = newTrigger()
 * .startNow()
 * .withSchedule(times)
 * .build();
 * Здесь можно указать, когда начинать запуск. Мы хотим сделать это сразу.
 * <p>
 * 5. Загрузка задачи и триггера в планировщик
 * scheduler.scheduleJob(job, trigger);
 */

public class AlertRabbit {
    public static void main(String[] args) {
        try {
            int interval = Integer.parseInt(loadProperties().getProperty("rabbit.interval"));
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private static Properties loadProperties() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }
}