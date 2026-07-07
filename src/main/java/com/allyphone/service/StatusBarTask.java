package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.item.PhoneItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Periodically shows each online player carrying a phone their signal strength and unread
 * notification count on the action bar (directly above the hotbar - the closest vanilla
 * equivalent to a persistent bottom-of-screen HUD, since Bukkit has no true bottom-left overlay),
 * and refreshes the phone item's lore/glow to reflect their unread message count.
 *
 * The signal readout itself is deliberately not shown all the time - only when there's actually
 * something to react to: no service at all (persistent until regained), the moment reception
 * drops (a one-off warning), or while the player is holding the phone (so they can check it on
 * demand). Unread notifications piggyback on the same action bar line whenever there are any,
 * independent of the signal conditions above, since only one action bar message can show at once.
 *
 * Runs well inside the ~3s the client keeps an action bar message visible, so consecutive resends
 * overlap with no gap - otherwise a "persistent" state would visibly flash off and back on.
 *
 * Set 'phone.actionbar-enabled: false' in config.yml to turn AllyPhone's own action bar text off
 * entirely - e.g. if you're rendering signal/notifications through another plugin's HUD instead
 * (such as CustomNameplates' actionbar.yml/bossbar.yml) via the %allyphone_...% PlaceholderAPI
 * placeholders. Two plugins both calling Player#sendActionBar independently will fight over the
 * same line, so only one should own it at a time.
 */
public class StatusBarTask extends BukkitRunnable {

    private final AllyPhonePlugin plugin;
    private final Map<UUID, Integer> lastSignal = new ConcurrentHashMap<>();

    public StatusBarTask(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        boolean actionBarEnabled = plugin.getConfig().getBoolean("phone.actionbar-enabled", true);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            int slot = plugin.getPhoneService().findPhoneSlot(player);
            if (slot < 0) continue;

            int signal = plugin.getSignalService().getSignalStrength(player);
            Integer previous = lastSignal.put(player.getUniqueId(), signal);

            int unreadMessages = plugin.getMessageService().getUnreadCount(player.getUniqueId());
            int unreadAlerts = plugin.getAlertService().getUnreadCount(player.getUniqueId());

            if (actionBarEnabled) {
                boolean noService = signal == 0;
                boolean justDropped = previous != null && signal < previous;
                boolean holdingPhone = PhoneItem.isPhone(plugin, player.getInventory().getItemInMainHand());

                String signalPart = null;
                if (noService || holdingPhone || justDropped) {
                    String bars = signal > 0
                            ? "§a" + "▮".repeat(signal) + "§7" + "▯".repeat(5 - signal)
                            : "§cNo Signal";
                    signalPart = "📶 " + bars;
                }

                int unreadTotal = unreadMessages + unreadAlerts;
                String bellPart = unreadTotal > 0 ? "§e🔔 §f" + unreadTotal : null;

                if (signalPart != null || bellPart != null) {
                    String message = "§f" + (signalPart != null ? signalPart : "")
                            + (signalPart != null && bellPart != null ? "  " : "")
                            + (bellPart != null ? bellPart : "");
                    player.sendActionBar(message);
                }
            }

            ItemStack phone = player.getInventory().getItem(slot);
            player.getInventory().setItem(slot, PhoneItem.applyUnreadBadge(plugin, phone, unreadMessages));
        }
    }
}
