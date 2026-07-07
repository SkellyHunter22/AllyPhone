package com.allyphone.papi;

import com.allyphone.AllyPhonePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class AllyPhoneExpansion extends PlaceholderExpansion {

    private final AllyPhonePlugin plugin;

    public AllyPhoneExpansion(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "allyphone";
    }

    @Override
    public String getAuthor() {
        return "SkellyHunter22";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";

        try {
            return switch (params.toLowerCase()) {
                case "signal" -> String.valueOf(plugin.getSignalService().getSignalStrength(player));
                case "signal_bars" -> signalBars(plugin.getSignalService().getSignalStrength(player));
                case "plan" -> plugin.getServicePlanService().getPlan(player.getUniqueId()).getDisplayName();
                case "service_active" ->
                        plugin.getServicePlanService().isServiceActive(player.getUniqueId()) ? "yes" : "no";
                case "unread_messages" -> String.valueOf(plugin.getMessageService().getUnreadCount(player.getUniqueId()));
                case "unread_alerts" -> String.valueOf(plugin.getAlertService().getUnreadCount(player.getUniqueId()));
                case "unread_total" -> String.valueOf(plugin.getMessageService().getUnreadCount(player.getUniqueId())
                        + plugin.getAlertService().getUnreadCount(player.getUniqueId()));
                default -> null;
            };
        } catch (SQLException e) {
            return "";
        }
    }

    /** e.g. "§a▮▮▮▯▯" for 3/5 bars, or "§cNo Signal" at 0 - handy for another plugin's HUD config. */
    private String signalBars(int signal) {
        return signal > 0
                ? "§a" + "▮".repeat(signal) + "§7" + "▯".repeat(5 - signal)
                : "§cNo Signal";
    }
}
