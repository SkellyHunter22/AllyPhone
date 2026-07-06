package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.List;

public class JoinListener implements Listener {

    private static final List<String> DEFAULT_APPS = List.of(
            "alerts", "wallet", "friends", "messages", "news", "weather",
            "servers", "towny", "plots", "appstore", "help");

    private final AllyPhonePlugin plugin;

    public JoinListener(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            plugin.getServicePlanService().ensureAccount(player.getUniqueId());
            plugin.getInstalledAppsStore().installDefaultsIfEmpty(player.getUniqueId(), DEFAULT_APPS);
        } catch (SQLException e) {
            plugin.getLogger().warning("JoinListener: failed to initialize phone account for "
                    + player.getName() + ": " + e.getMessage());
        }
        plugin.getPhoneService().deliverPhone(player);
    }
}
