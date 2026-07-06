package com.allyphone.service;

import com.allyphone.sql.Database;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD for the {@code atms} table (physical cash withdrawal happens in AlsBanker; this just locates them). */
public class AtmStore {

    public record Atm(long id, String name, String world, int x, int y, int z) {
    }

    private final Database database;
    private List<Atm> cache;

    public AtmStore(Database database) {
        this.database = database;
    }

    public synchronized void add(String name, Location loc) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT INTO atms (name, world, x, y, z, created_at) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, name);
            ps.setString(2, loc.getWorld().getName());
            ps.setInt(3, loc.getBlockX());
            ps.setInt(4, loc.getBlockY());
            ps.setInt(5, loc.getBlockZ());
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        }
        cache = null;
    }

    public synchronized boolean removeByName(String name) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "DELETE FROM atms WHERE name = ?")) {
            ps.setString(1, name);
            boolean removed = ps.executeUpdate() > 0;
            cache = null;
            return removed;
        }
    }

    public synchronized List<Atm> getAll() throws SQLException {
        if (cache != null) {
            return cache;
        }
        List<Atm> out = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM atms");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Atm(rs.getLong("id"), rs.getString("name"), rs.getString("world"),
                        rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
            }
        }
        cache = out;
        return out;
    }
}
