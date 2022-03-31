package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * <p>
 * 1. Сначала мы получаем страницу, чтобы с ней можно было работать:
 * Connection connection = Jsoup.connect(PAGE_LINK);
 * Document document = connection.get();
 * <p>
 * 2. Получаем данные по заданному селектору
 * Elements rows = document.select(".vacancy-card__inner");
 * Обратите внимание, что перед CSS классом ставится точка.
 * Это правила CSS селекторов, с которыми работает метод JSOUP select()
 * <p>
 * 3. С помощью цикла перебираем полученные элементы.  Сначала получаем элементы
 * содержащие название и ссылку. Стоит обратить внимание, что дочерние элементы
 * можно получать через индекс - метод child(0) или же через селектор - select(".vacancy-card__title").
 * Element titleElement = row.select(".vacancy-card__title").first();
 * Element linkElement = titleElement.child(0);
 * <p>
 * 4. С помощью text() можно  получить текст, с помощью attr("имя атрибута") атрибуты.
 * String vacancyName = titleElement.text();
 * linkElement.attr("href");
 */

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        Connection connection = Jsoup.connect(PAGE_LINK);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            Element dateElement = row.select(".vacancy-card__date").first();
            Element timeElement = dateElement.child(0);
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            System.out.printf("%s %s %s%n", vacancyName, link, timeElement.attr("datetime"));
        });
    }
}