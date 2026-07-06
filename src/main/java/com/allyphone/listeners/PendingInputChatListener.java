package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/** Captures the next chat line from a player awaiting free-text input (e.g. renaming their phone). */
public class PendingInputChatListener implements Listener {

    private final AllyPhonePlugin plugin;

    public PendingInputChatListener(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPendingInputService().isPending(player.getUniqueId())) return;

        event.setCancelled(true);
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getPendingInputService().consume(player.getUniqueId(), message));
    }
}
