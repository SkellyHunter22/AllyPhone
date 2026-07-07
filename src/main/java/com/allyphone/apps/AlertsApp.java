package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import com.allyphone.sql.AlertSQLService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;

public class AlertsApp implements PhoneApp {

    private static final int MAX_ALERTS_SHOWN = 14;

    @Override
    public String getId() {
        return "alerts";
    }

    @Override
    public String getDisplayName() {
        return "§c⚠ Alerts";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        int unread = AllyPhonePlugin.get().getAlertService().getUnreadCount(viewer.getUniqueId());
        String badge = unread > 0 ? " §c(" + unread + ")" : "";
        return GuiUtil.icon("system_status_warnings", getDisplayName() + badge, "§7System status & warnings");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public boolean isEssential() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, "§c§lAlerts");
        holder.setInventory(inv);

        int signal = plugin.getSignalService().getSignalStrength(player);
        boolean active;
        try {
            active = plugin.getServicePlanService().isServiceActive(player.getUniqueId());
        } catch (SQLException e) {
            active = true;
        }

        inv.setItem(0, GuiUtil.icon(signal > 0 ? "signal_ok" : "no_signal",
                signal > 0 ? "§aSignal OK (" + signal + "/5)" : "§cNo Signal",
                "§7You are " + (signal > 0 ? "in" : "out of") + " coverage."));

        inv.setItem(1, GuiUtil.icon(active ? "billing_active" : "service_suspended",
                active ? "§aBilling Active" : "§cService Suspended",
                active ? "§7Your account is in good standing." : "§7Pay your bill to restore service."));

        inv.setItem(2, GuiUtil.icon("server_online", "§bServer Online",
                "§7" + Bukkit.getOnlinePlayers().size() + " players online"));

        List<AlertSQLService.StoredAlert> alerts = plugin.getAlertService().getRecent(player.getUniqueId(), MAX_ALERTS_SHOWN);
        if (alerts.isEmpty()) {
            inv.setItem(13, GuiUtil.icon("no_alerts", "§7No alerts", "§8Nothing to report right now."));
        } else {
            int slot = 9;
            for (AlertSQLService.StoredAlert alert : alerts) {
                if (slot >= 27) break;
                inv.setItem(slot, GuiUtil.icon(
                        alert.read() ? "alert_read" : "alert_unread",
                        (alert.read() ? "§7" : "§e") + alert.message(),
                        "§8From: " + alert.source(),
                        "§8" + timeAgo(alert.createdAt())));
                slot++;
            }
        }

        inv.setItem(31, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);

        plugin.getAlertService().markAllRead(player.getUniqueId());
    }

    private static String timeAgo(long createdAtMillis) {
        long minutes = (System.currentTimeMillis() - createdAtMillis) / 60000L;
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        return (hours / 24) + "d ago";
    }
}
