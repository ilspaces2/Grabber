package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HarbCareerDateTimeParser;
import ru.job4j.model.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private final DateTimeParser dateTimeParser;

    private int numPage = 1;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HarbCareerDateTimeParser());
        habrCareerParse.setNumPage(5);
        habrCareerParse.list(PAGE_LINK).forEach(System.out::println);
    }

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        String text = null;
        try {
            Document document = connection.get();
            Element description = document.select(".job_show_description").first();
            Element descriptionChild = description.child(0);
            text = descriptionChild.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    @Override
    public List<Post> list(String link) {
        if (numPage < 1) {
            throw new IllegalArgumentException("NumPage less 1");
        }
        List<Post> posts = new ArrayList<>();
        try {
            for (int count = 1; count <= numPage; count++) {
                Connection connection = Jsoup.connect(link + "?page=" + count);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    Element dateElement = row.select(".vacancy-card__date").first();
                    Element timeElement = dateElement.child(0);
                    String linkPost = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    posts.add(new Post(
                            vacancyName,
                            linkPost,
                            retrieveDescription(linkPost),
                            dateTimeParser.parse(timeElement.attr("datetime"))
                    ));
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.copyOf(posts);
    }

    public int getNumPage() {
        return numPage;
    }

    public void setNumPage(int numPage) {
        this.numPage = numPage;
    }
}