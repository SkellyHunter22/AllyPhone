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
            return Math.max(1, (int) Math.ceil(ratio * 5));
        } catch (SQLException e) {
            plugin.getLogger().warning("SignalService: failed to read cell towers: " + e.getMessage());
            return 5;
        }
    }

    public boolean hasService(Player player) {
        return getSignalStrength(player) > 0;
    }
}
