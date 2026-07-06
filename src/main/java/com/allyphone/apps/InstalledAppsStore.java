package com.allyphone.apps;

import com.allyphone.sql.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Tracks which optional apps a player has installed via the {@code installed_apps} table. */
public class InstalledAppsStore {

    private final Database database;

    public InstalledAppsStore(Database database) {
        this.database = database;
    }

    public synchronized void install(UUID uuid, String appId) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT OR IGNORE INTO installed_apps (uuid, app_id) VALUES (?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, appId.toLowerCase());
            ps.executeUpdate();
        }
    }

    public synchronized void uninstall(UUID uuid, String appId) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "DELETE FROM installed_apps WHERE uuid = ? AND app_id = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, appId.toLowerCase());
            ps.executeUpdate();
        }
    }

    public synchronized Set<String> getInstalled(UUID uuid) throws SQLException {
        Set<String> ids = new HashSet<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT app_id FROM installed_apps WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString("app_id"));
                }
            }
        }
        return ids;
    }

    /** Installs the given defaults the first time a player is seen (i.e. they have nothing installed yet). */
    public synchronized void installDefaultsIfEmpty(UUID uuid, Iterable<String> defaults) throws SQLException {
        if (!getInstalled(uuid).isEmpty()) return;
        for (String id : defaults) {
            install(uuid, id);
        }
    }
}
