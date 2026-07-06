package com.allyphone.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD for the {@code news} table. */
public class NewsSQLService {

    public record NewsPost(long id, String author, String title, String body, long postedAt) {
    }

    private final Database database;

    public NewsSQLService(Database database) {
        this.database = database;
    }

    public synchronized void insert(String author, String title, String body) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT INTO news (author, title, body, posted_at) VALUES (?,?,?,?)")) {
            ps.setString(1, author);
            ps.setString(2, title);
            ps.setString(3, body == null ? "" : body);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    public synchronized List<NewsPost> getRecent(int limit) throws SQLException {
        List<NewsPost> out = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT * FROM news ORDER BY posted_at DESC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new NewsPost(
                            rs.getLong("id"),
                            rs.getString("author"),
                            rs.getString("title"),
                            rs.getString("body"),
                            rs.getLong("posted_at")));
                }
            }
        }
        return out;
    }
}
