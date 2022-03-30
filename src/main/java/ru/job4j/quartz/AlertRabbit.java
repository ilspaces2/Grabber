package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
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
 * 2.1 JobDataMap data = new JobDataMap();
 * data.put("ключ вызова ресурса", ресурс с которым job будет работать);
 * JobDetail job = newJob(Rabbit.class)
 * .usingJobData(data)
 * .build();
 * В классе исполняющем Job метод принимает параметр JobExecutionContext context.
 * В методе для получения ресурса : Тип имя = (Тип) context.getJobDetail().getJobDataMap().get("ключ");
 *
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
        Properties properties = loadProperties();
        try {
            Class.forName(properties.getProperty("driver-class-name"));
            try (Connection conn = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"))) {
                int interval = Integer.parseInt(properties.getProperty("rabbit.interval"));
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("connect", conn);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(interval)
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
            }
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
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connect");
            try (PreparedStatement ps = connection.prepareStatement("insert into rabbit (created_date) values(?)")) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}