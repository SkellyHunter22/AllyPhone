package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Renders each cell tower's coverage radius as a particle ring around the player, at the
 * player's own altitude - the reception equivalent of how land-claim plugins outline borders.
 */
public class CellTowerVisualizer {

    private static final int POINTS_PER_RING = 120;
    private static final double VISIBLE_RANGE = 80.0;

    private final AllyPhonePlugin plugin;
    private final Set<UUID> active = new HashSet<>();
    private BukkitTask task;

    public CellTowerVisualizer(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /** Flips a player's coverage overlay on/off. Returns the new state (true = now showing). */
    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (!active.add(uuid)) {
            active.remove(uuid);
            stopTaskIfIdle();
            return false;
        }
        ensureTaskRunning();
        return true;
    }

    public boolean isActive(Player player) {
        return active.contains(player.getUniqueId());
    }

    public void remove(Player player) {
        active.remove(player.getUniqueId());
        stopTaskIfIdle();
    }

    public void shutdown() {
        active.clear();
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void ensureTaskRunning() {
        if (task != null) return;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void stopTaskIfIdle() {
        if (active.isEmpty() && task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        if (active.isEmpty()) return;

        List<CellTowerStore.CellTower> towers;
        try {
            towers = plugin.getCellTowerStore().getAll();
        } catch (SQLException e) {
            return;
        }
        if (towers.isEmpty()) return;

        for (UUID uuid : active) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null) continue;
            for (CellTowerStore.CellTower tower : towers) {
                if (!tower.world().equals(player.getWorld().getName())) continue;
                drawRing(player, tower);
            }
        }
    }

    private void drawRing(Player player, CellTowerStore.CellTower tower) {
        Location playerLoc = player.getLocation();
        double dx = tower.x() - playerLoc.getX();
        double dz = tower.z() - playerLoc.getZ();
        double distToCenter = Math.sqrt(dx * dx + dz * dz);
        // Skip towers whose ring can't possibly touch the visible range around the player.
        if (Math.abs(distToCenter - tower.radius()) > VISIBLE_RANGE) return;

        boolean inCoverage = distToCenter <= tower.radius();
        Particle.DustOptions dust = new Particle.DustOptions(
                inCoverage ? Color.fromRGB(60, 220, 100) : Color.fromRGB(220, 60, 60), 1.1f);

        double y = playerLoc.getY() + 0.1;
        double visibleRangeSq = VISIBLE_RANGE * VISIBLE_RANGE;
        for (int i = 0; i < POINTS_PER_RING; i++) {
            double angle = 2 * Math.PI * i / POINTS_PER_RING;
            double x = tower.x() + tower.radius() * Math.cos(angle);
            double z = tower.z() + tower.radius() * Math.sin(angle);
            double pdx = x - playerLoc.getX();
            double pdz = z - playerLoc.getZ();
            if (pdx * pdx + pdz * pdz > visibleRangeSq) continue;
            player.spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0, dust);
        }
    }
}
