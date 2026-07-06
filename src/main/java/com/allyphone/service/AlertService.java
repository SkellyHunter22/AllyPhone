package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.sql.AlertSQLService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Central inbox other plugins (e.g. AlsBanker) push notifications into via the
 * {@code phonealert} console command, and that the Alerts app renders.
 */
public class AlertService {

    private final AllyPhonePlugin plugin;
    private final AlertSQLService alertSqlService;

    public AlertService(AllyPhonePlugin plugin, AlertSQLService alertSqlService) {
        this.plugin = plugin;
        this.alertSqlService = alertSqlService;
    }

    public void push(UUID uuid, String source, String message) {
        try {
            alertSqlService.insert(uuid, source, message);
        } catch (SQLException e) {
            plugin.getLogger().warning("AlertService: failed to store alert: " + e.getMessage());
            return;
        }

        Player online = plugin.getServer().getPlayer(uuid);
        if (online != null && online.isOnline()) {
            online.sendMessage("§c§l[Alert] §f" + message);
            online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        }
    }

    public List<AlertSQLService.StoredAlert> getRecent(UUID uuid, int limit) {
        try {
            return alertSqlService.getRecent(uuid, limit);
        } catch (SQLException e) {
            plugin.getLogger().warning("AlertService: failed to load alerts: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public int getUnreadCount(UUID uuid) {
        try {
            return alertSqlService.countUnread(uuid);
        } catch (SQLException e) {
            plugin.getLogger().warning("AlertService: failed to count unread alerts: " + e.getMessage());
            return 0;
        }
    }

    public void markAllRead(UUID uuid) {
        try {
            alertSqlService.markAllRead(uuid);
        } catch (SQLException e) {
            plugin.getLogger().warning("AlertService: failed to mark alerts read: " + e.getMessage());
        }
    }
}
