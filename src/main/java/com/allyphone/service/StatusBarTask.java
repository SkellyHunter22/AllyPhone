package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.item.PhoneItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodically shows each online player carrying a phone their signal strength on the action bar,
 * and refreshes the phone item's lore/glow to reflect their unread message count.
 */
public class StatusBarTask extends BukkitRunnable {

    private final AllyPhonePlugin plugin;

    public StatusBarTask(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            int slot = plugin.getPhoneService().findPhoneSlot(player);
            if (slot < 0) continue;

            int signal = plugin.getSignalService().getSignalStrength(player);
            String bars = signal > 0
                    ? "§a" + "▮".repeat(signal) + "§7" + "▯".repeat(5 - signal)
                    : "§cNo Signal";
            player.sendActionBar("§f📶 " + bars);

            int unread = plugin.getMessageService().getUnreadCount(player.getUniqueId());
            ItemStack phone = player.getInventory().getItem(slot);
            player.getInventory().setItem(slot, PhoneItem.applyUnreadBadge(plugin, phone, unread));
        }
    }
}
