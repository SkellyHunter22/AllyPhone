package com.allyphone.service;

import com.allyphone.sql.Database;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** CRUD + lookup for the {@code cell_towers} table. */
public class CellTowerStore {

    public record CellTower(long id, String name, String world, int x, int y, int z, int radius, UUID owner) {
    }

    private final Database database;
    private List<CellTower> cache;

    public CellTowerStore(Database database) {
        this.database = database;
    }

    public synchronized void add(String name, Location loc, int radius, UUID owner) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT INTO cell_towers (name, world, x, y, z, radius, owner_uuid, created_at) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, name);
            ps.setString(2, loc.getWorld().getName());
            ps.setInt(3, loc.getBlockX());
            ps.setInt(4, loc.getBlockY());
            ps.setInt(5, loc.getBlockZ());
            ps.setInt(6, radius);
            ps.setString(7, owner != null ? owner.toString() : null);
            ps.setLong(8, System.currentTimeMillis());
            ps.executeUpdate();
        }
        cache = null;
    }

    public synchronized boolean removeByName(String name) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "DELETE FROM cell_towers WHERE name = ?")) {
            ps.setString(1, name);
            boolean removed = ps.executeUpdate() > 0;
            cache = null;
            return removed;
        }
    }

    /** Cached in memory; invalidated on add/remove so callers never hit the DB on every tick. */
    public synchronized List<CellTower> getAll() throws SQLException {
        if (cache != null) {
            return cache;
        }
        List<CellTower> out = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM cell_towers");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(read(rs));
            }
        }
        cache = out;
        return out;
    }

    /** Nearest tower covering a location (distance within its radius), or null if none in range. */
    public CellTower findCovering(Location loc) throws SQLException {
        CellTower nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (CellTower tower : getAll()) {
            if (!tower.world().equals(loc.getWorld().getName())) continue;
            double dist = distance(tower, loc);
            if (dist <= tower.radius() && dist < nearestDist) {
                nearest = tower;
                nearestDist = dist;
            }
        }
        return nearest;
    }

    private double distance(CellTower tower, Location loc) {
        double dx = tower.x() - loc.getX();
        double dy = tower.y() - loc.getY();
        double dz = tower.z() - loc.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private CellTower read(ResultSet rs) throws SQLException {
        String ownerStr = rs.getString("owner_uuid");
        return new CellTower(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("world"),
                rs.getInt("x"),
                rs.getInt("y"),
                rs.getInt("z"),
                rs.getInt("radius"),
                ownerStr != null ? UUID.fromString(ownerStr) : null);
    }
}
