package com.allyphone.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** CRUD for the {@code messages} table. */
public class MessageSQLService {

    public record StoredMessage(long id, UUID senderUuid, String senderName, UUID receiverUuid,
                                String receiverName, String body, long sentAt, boolean read) {
    }

    private final Database database;

    public MessageSQLService(Database database) {
        this.database = database;
    }

    public synchronized void insert(UUID senderUuid, String senderName, UUID receiverUuid,
                                    String receiverName, String body) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT INTO messages (sender_uuid, sender_name, receiver_uuid, receiver_name, body, sent_at) "
                        + "VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, senderUuid.toString());
            ps.setString(2, senderName);
            ps.setString(3, receiverUuid.toString());
            ps.setString(4, receiverName);
            ps.setString(5, body);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    public synchronized List<StoredMessage> getInbox(UUID receiver, int limit) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT * FROM messages WHERE receiver_uuid = ? ORDER BY sent_at DESC LIMIT ?")) {
            ps.setString(1, receiver.toString());
            ps.setInt(2, limit);
            return readAll(ps);
        }
    }

    public synchronized int countUnread(UUID receiver) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM messages WHERE receiver_uuid = ? AND read = 0")) {
            ps.setString(1, receiver.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public synchronized void markAllRead(UUID receiver) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE messages SET read = 1 WHERE receiver_uuid = ?")) {
            ps.setString(1, receiver.toString());
            ps.executeUpdate();
        }
    }

    private List<StoredMessage> readAll(PreparedStatement ps) throws SQLException {
        List<StoredMessage> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new StoredMessage(
                        rs.getLong("id"),
                        UUID.fromString(rs.getString("sender_uuid")),
                        rs.getString("sender_name"),
                        UUID.fromString(rs.getString("receiver_uuid")),
                        rs.getString("receiver_name"),
                        rs.getString("body"),
                        rs.getLong("sent_at"),
                        rs.getInt("read") == 1));
            }
        }
        return out;
    }
}
