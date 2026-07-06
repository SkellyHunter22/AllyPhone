package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    private final AllyPhonePlugin plugin;

    public RespawnListener(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.getPhoneService().deliverPhone(event.getPlayer());
    }
}
