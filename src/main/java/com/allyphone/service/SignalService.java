package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class SignalService {

    private final AllyPhonePlugin plugin;

    public SignalService(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /** 0-5 bars. 0 means no service. */
    public int getSignalStrength(Player player) {
        if (!plugin.getConfig().getBoolean("phone.towers-enabled", true)) {
            return 5;
        }
        try {
            CellTowerStore.CellTower tower = plugin.getCellTowerStore().findCovering(player.getLocation());
            if (tower == null) {
                return 0;
            }
            Location towerLoc = new Location(player.getWorld(), tower.x(), tower.y(), tower.z());
            double ratio = 1 - (player.getLocation().distance(towerLoc) / tower.radius());
            int bars = Math.max(1, (int) Math.ceil(ratio * 5));
            bars -= obstructionPenalty(player);
            return Math.max(0, Math.min(5, bars));
        } catch (SQLException e) {
            plugin.getLogger().warning("SignalService: failed to read cell towers: " + e.getMessage());
            return 5;
        }
    }

    /**
     * Bars lost to being roofed over (a building ceiling or cave roof block the sky, regardless of
     * time of day - sky light reflects line-of-sight to open air, not brightness) and to how deep
     * underground the player has dug relative to the surface. Both are config-tunable per server.
     */
    private int obstructionPenalty(Player player) {
        int penalty = 0;

        if (player.getLocation().getBlock().getLightFromSky() < 15) {
            penalty += plugin.getConfig().getInt("phone.signal-indoor-penalty", 1);
        }

        int surfaceY = player.getWorld().getHighestBlockYAt(player.getLocation());
        int depth = surfaceY - player.getLocation().getBlockY();
        if (depth > 0) {
            int depthPerBar = Math.max(1, plugin.getConfig().getInt("phone.signal-depth-per-bar", 15));
            int maxDepthPenalty = plugin.getConfig().getInt("phone.signal-max-depth-penalty", 3);
            penalty += Math.min(maxDepthPenalty, depth / depthPerBar);
        }

        return penalty;
    }

    public boolean hasService(Player player) {
        return getSignalStrength(player) > 0;
    }
}
