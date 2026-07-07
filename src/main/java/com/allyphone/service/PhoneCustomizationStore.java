package com.allyphone.service;

import com.allyphone.sql.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Per-player phone cosmetics: bezel theme, phone item nickname, and home-screen app order. */
public class PhoneCustomizationStore {

    /** Bezel theme options, keyed by name, mapped to their stained glass pane material name in GuiUtil. */
    public enum Theme {
        BLACK("§7Black", "BLACK_STAINED_GLASS_PANE"),
        BLUE("§9Blue", "BLUE_STAINED_GLASS_PANE"),
        RED("§cRed", "RED_STAINED_GLASS_PANE"),
        GREEN("§aGreen", "GREEN_STAINED_GLASS_PANE"),
        PURPLE("§5Purple", "PURPLE_STAINED_GLASS_PANE"),
        LIGHT_GRAY("§fLight Gray", "LIGHT_GRAY_STAINED_GLASS_PANE");

        public final String label;
        public final String materialName;

        Theme(String label, String materialName) {
            this.label = label;
            this.materialName = materialName;
        }
    }

    private final Database database;
    /** Themes are read on every GUI screen open (for the bezel), so cache them to avoid a JDBC round-trip per click. */
    private final Map<UUID, Theme> themeCache = new ConcurrentHashMap<>();

    public PhoneCustomizationStore(Database database) {
        this.database = database;
    }

    public Theme getTheme(UUID uuid) throws SQLException {
        Theme cached = themeCache.get(uuid);
        if (cached != null) return cached;

        Theme theme = loadTheme(uuid);
        themeCache.put(uuid, theme);
        return theme;
    }

    private synchronized Theme loadTheme(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT theme FROM phone_prefs WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Theme.BLACK;
                try {
                    return Theme.valueOf(rs.getString("theme"));
                } catch (IllegalArgumentException e) {
                    return Theme.BLACK;
                }
            }
        }
    }

    public synchronized void setTheme(UUID uuid, Theme theme) throws SQLException {
        upsert(uuid);
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE phone_prefs SET theme = ? WHERE uuid = ?")) {
            ps.setString(1, theme.name());
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
        themeCache.put(uuid, theme);
    }

    public synchronized String getNickname(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT nickname FROM phone_prefs WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("nickname") : null;
            }
        }
    }

    public synchronized void setNickname(UUID uuid, String nickname) throws SQLException {
        upsert(uuid);
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE phone_prefs SET nickname = ? WHERE uuid = ?")) {
            ps.setString(1, nickname);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    /** Returns the player's saved app order (ids, front to back), or an empty list if none saved yet. */
    public synchronized List<String> getAppOrder(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT app_order FROM phone_prefs WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new ArrayList<>();
                String raw = rs.getString("app_order");
                if (raw == null || raw.isBlank()) return new ArrayList<>();
                return new ArrayList<>(Arrays.asList(raw.split(",")));
            }
        }
    }

    public synchronized void setAppOrder(UUID uuid, List<String> order) throws SQLException {
        upsert(uuid);
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE phone_prefs SET app_order = ? WHERE uuid = ?")) {
            ps.setString(1, String.join(",", order));
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    /** Drops the cached theme for a player, e.g. on disconnect. */
    public void evictThemeCache(UUID uuid) {
        themeCache.remove(uuid);
    }

    private void upsert(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT OR IGNORE INTO phone_prefs (uuid) VALUES (?)")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }
}
