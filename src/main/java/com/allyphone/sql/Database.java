package com.allyphone.sql;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite database for AllyPhone. Owns a single connection (SQLite is
 * single-writer) and creates the schema on first run.
 */
public class Database {

    private final Plugin plugin;
    private Connection connection;

    public Database(Plugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void init() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new SQLException("Could not create plugin data folder");
        }
        File dbFile = new File(dataFolder, "allyphone.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sender_uuid TEXT NOT NULL,
                        sender_name TEXT NOT NULL,
                        receiver_uuid TEXT NOT NULL,
                        receiver_name TEXT NOT NULL,
                        body TEXT NOT NULL,
                        sent_at INTEGER NOT NULL,
                        read INTEGER NOT NULL DEFAULT 0
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS news (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        author TEXT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL DEFAULT '',
                        posted_at INTEGER NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS cell_towers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        world TEXT NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        radius INTEGER NOT NULL,
                        owner_uuid TEXT,
                        created_at INTEGER NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS phone_accounts (
                        uuid TEXT PRIMARY KEY,
                        plan TEXT NOT NULL DEFAULT 'BASIC',
                        last_billed INTEGER NOT NULL DEFAULT 0,
                        service_active INTEGER NOT NULL DEFAULT 1
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS installed_apps (
                        uuid TEXT NOT NULL,
                        app_id TEXT NOT NULL,
                        PRIMARY KEY (uuid, app_id)
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS atms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        world TEXT NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        created_at INTEGER NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS phone_prefs (
                        uuid TEXT PRIMARY KEY,
                        theme TEXT NOT NULL DEFAULT 'BLACK',
                        nickname TEXT,
                        app_order TEXT NOT NULL DEFAULT ''
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS phone_alerts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        uuid TEXT NOT NULL,
                        source TEXT NOT NULL,
                        message TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        read INTEGER NOT NULL DEFAULT 0
                    )""");
        }
    }

    public synchronized Connection getConnection() {
        return connection;
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to close database: " + e.getMessage());
            }
            connection = null;
        }
    }
}
