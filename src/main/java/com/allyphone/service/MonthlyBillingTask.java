package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

/** Periodically checks online players' billing cycle and charges/suspends service as needed. */
public class MonthlyBillingTask extends BukkitRunnable {

    private static final long CYCLE_MILLIS = 30L * 24 * 60 * 60 * 1000;

    private final AllyPhonePlugin plugin;

    public MonthlyBillingTask(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                long lastBilled = plugin.getServicePlanService().getLastBilled(player.getUniqueId());
                if (System.currentTimeMillis() - lastBilled < CYCLE_MILLIS) continue;

                ServicePlan plan = plugin.getServicePlanService().getPlan(player.getUniqueId());
                boolean charged = plugin.getBillingService().charge(player, plan.getMonthlyCost());
                plugin.getServicePlanService().setServiceActive(player.getUniqueId(), charged);
                plugin.getServicePlanService().setLastBilled(player.getUniqueId(), System.currentTimeMillis());

                if (charged) {
                    player.sendMessage("§7[AllyPhone] Billed $" + plan.getMonthlyCost() + " for your "
                            + plan.getDisplayName() + " plan.");
                } else {
                    player.sendMessage("§c[AllyPhone] Billing failed - your service has been suspended until you pay.");
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("MonthlyBillingTask: failed to bill " + player.getName() + ": " + e.getMessage());
            }
        }
    }
}
