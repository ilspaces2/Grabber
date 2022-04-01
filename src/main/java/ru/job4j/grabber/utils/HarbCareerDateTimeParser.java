package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HarbCareerDateTimeParser implements DateTimeParser {
    @Override
    public LocalDateTime parse(String parse) {
        return LocalDateTime.parse(parse, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }
}
