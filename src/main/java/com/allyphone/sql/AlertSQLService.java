package com.allyphone.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** CRUD for the {@code phone_alerts} table. */
public class AlertSQLService {

    public record StoredAlert(long id, UUID uuid, String source, String message, long createdAt, boolean read) {
    }

    private final Database database;

    public AlertSQLService(Database database) {
        this.database = database;
    }

    public synchronized void insert(UUID uuid, String source, String message) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT INTO phone_alerts (uuid, source, message, created_at) VALUES (?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, source);
            ps.setString(3, message);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    public synchronized List<StoredAlert> getRecent(UUID uuid, int limit) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT * FROM phone_alerts WHERE uuid = ? ORDER BY created_at DESC LIMIT ?")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, limit);
            return readAll(ps);
        }
    }

    public synchronized int countUnread(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM phone_alerts WHERE uuid = ? AND read = 0")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public synchronized void markAllRead(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE phone_alerts SET read = 1 WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    private List<StoredAlert> readAll(PreparedStatement ps) throws SQLException {
        List<StoredAlert> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new StoredAlert(
                        rs.getLong("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("source"),
                        rs.getString("message"),
                        rs.getLong("created_at"),
                        rs.getInt("read") == 1));
            }
        }
        return out;
    }
}
