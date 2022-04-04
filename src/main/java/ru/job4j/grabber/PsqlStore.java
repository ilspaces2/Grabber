package ru.job4j.grabber;

import ru.job4j.model.Post;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement prepare = cnn.prepareStatement(
                "insert into post (name,text,link,created) values (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            prepare.setString(1, post.getTitle());
            prepare.setString(2, post.getDescription());
            prepare.setString(3, post.getLink());
            prepare.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            prepare.executeUpdate();
            try (ResultSet resultSet = prepare.getGeneratedKeys()) {
                if (resultSet.next()) {
                    post.setId(resultSet.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement prepare = cnn.prepareStatement("select * from post")) {
            prepare.executeQuery();
            try (ResultSet resultSet = prepare.getResultSet()) {
                while (resultSet.next()) {
                    posts.add(new Post(
                            resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getTimestamp(5).toLocalDateTime()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.copyOf(posts);
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement prepare = cnn.prepareStatement("select * from post where id=?")) {
            prepare.setInt(1, id);
            prepare.executeQuery();
            try (ResultSet resultSet = prepare.getResultSet()) {
                if (resultSet.next()) {
                    post = new Post(
                            resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getTimestamp(5).toLocalDateTime()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private static Properties initProperties() {
        Properties config = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static void main(String[] args) {
        Post post1 = new Post("Java1", "www.java1.com", "java 8, PostgresSQL", LocalDateTime.now());
        Post post2 = new Post("Java3", "www.java1123.com", "maven", LocalDateTime.now());
        Post post3 = new Post("JavaEE", "www.java155555.com", "git", LocalDateTime.now());
        Post post4 = new Post("Java12", "www.java112313.com", "spring", LocalDateTime.now());

        try (PsqlStore psqlStore = new PsqlStore(initProperties())) {
            psqlStore.save(post1);
            psqlStore.save(post2);
            psqlStore.save(post3);
            psqlStore.save(post4);
            System.out.println("All");
            psqlStore.getAll().forEach(System.out::println);
            System.out.println("By ID");
            System.out.println(psqlStore.findById(3));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}