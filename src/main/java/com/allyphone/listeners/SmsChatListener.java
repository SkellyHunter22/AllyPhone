package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.service.ServicePlan;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;

/** Captures the next chat line from a player who clicked a friend to SMS, instead of broadcasting it as chat. */
public class SmsChatListener implements Listener {

    private final AllyPhonePlugin plugin;

    public SmsChatListener(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String targetName = plugin.getPendingSmsService().consume(player.getUniqueId());
        if (targetName == null) return;

        event.setCancelled(true);
        String message = event.getMessage();

        Bukkit.getScheduler().runTask(plugin, () -> handle(player, targetName, message));
    }

    private void handle(Player player, String targetName, String message) {
        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage("§7SMS cancelled.");
            return;
        }

        if (!plugin.getSignalService().hasService(player)) {
            player.sendMessage("§cYou have no signal here.");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cThat player has never joined this server.");
            return;
        }

        try {
            ServicePlan plan = plugin.getServicePlanService().getPlan(player.getUniqueId());
            if (plan.getSmsCost() > 0 && !plugin.getBillingService().charge(player, plan.getSmsCost())) {
                player.sendMessage("§cInsufficient funds to send SMS ($" + plan.getSmsCost() + ").");
                return;
            }
        } catch (SQLException e) {
            player.sendMessage("§cFailed to check your service plan.");
            return;
        }

        if (plugin.getMessageService().send(player, target, message)) {
            player.sendMessage("§aMessage sent to " + target.getName() + ".");
        } else {
            player.sendMessage("§cFailed to send message.");
        }
    }
}
