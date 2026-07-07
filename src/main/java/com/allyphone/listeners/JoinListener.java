package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.List;

public class JoinListener implements Listener {

    private static final List<String> DEFAULT_APPS = List.of(
            "alerts", "wallet", "friends", "messages", "news", "weather",
            "servers", "towny", "plots", "appstore", "help", "music", "customize");

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
            if (player.hasPermission(com.allyphone.apps.AdminApp.PERMISSION)) {
                plugin.getInstalledAppsStore().install(player.getUniqueId(), "admin");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("JoinListener: failed to initialize phone account for "
                    + player.getName() + ": " + e.getMessage());
        }
        // Delayed a tick so it runs after any other plugin's first-join kit/inventory setup
        // (e.g. Essentials kits), which would otherwise silently overwrite the delivered phone.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getPhoneService().deliverPhone(player);
            }
        }, 1L);

        if (plugin.getResourcePackHost().isReady()) {
            player.setResourcePack(
                    plugin.getResourcePackHost().getUrl(),
                    plugin.getResourcePackHost().getSha1Bytes(),
                    "§bAllyPhone uses a small resource pack for its phone icon.",
                    false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getCellTowerVisualizer().remove(event.getPlayer());
        plugin.getPendingInputService().cancel(event.getPlayer().getUniqueId());
        plugin.getPendingSmsService().consume(event.getPlayer().getUniqueId());
        plugin.getPhoneCustomizationStore().evictThemeCache(event.getPlayer().getUniqueId());
    }
}
