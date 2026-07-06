package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/** Logs computed signal strength for all online players; only scheduled when debug.signal is enabled. */
public class SignalDebugTask extends BukkitRunnable {

    private final AllyPhonePlugin plugin;

    public SignalDebugTask(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            int signal = plugin.getSignalService().getSignalStrength(player);
            plugin.getLogger().info("[SignalDebug] " + player.getName() + " -> " + signal + "/5 bars");
        }
    }
}
