package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.AtmListGUI;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.NoServiceGUI;
import com.allyphone.gui.PhoneGuiHolder;
import com.allyphone.service.ServicePlan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;

public class GuiClickListener implements Listener {

    private final AllyPhonePlugin plugin;

    public GuiClickListener(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PhoneGuiHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        String appId = GuiUtil.getTag(plugin, clicked, GuiUtil.APP_ID_KEY);
        if (appId != null) {
            openApp(player, appId);
            return;
        }

        String action = GuiUtil.getTag(plugin, clicked, GuiUtil.ACTION_KEY);
        if (action != null) {
            handleAction(player, action, event.getClick().isShiftClick());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof PhoneGuiHolder) {
            event.setCancelled(true);
        }
    }

    private void openApp(Player player, String appId) {
        PhoneApp app = plugin.getAppRegistry().getApp(appId);
        if (app == null) return;

        if (app.requiresService() && !hasService(player)) {
            NoServiceGUI.open(player, reasonFor(player));
            return;
        }
        app.open(player);
    }

    private void handleAction(Player player, String action, boolean shiftClick) {
        if (action.equals("back")) {
            plugin.getPhoneService().openPhone(player);
        } else if (action.startsWith("install:")) {
            toggleInstall(player, action.substring("install:".length()), true);
        } else if (action.startsWith("uninstall:")) {
            toggleInstall(player, action.substring("uninstall:".length()), false);
        } else if (action.startsWith("server:")) {
            connectToServer(player, action.substring("server:".length()));
        } else if (action.startsWith("removetower:")) {
            if (shiftClick) {
                removeTower(player, action.substring("removetower:".length()));
            }
        } else if (action.startsWith("command:")) {
            runCommand(player, action.substring("command:".length()));
        } else if (action.equals("paybill")) {
            payBill(player);
        } else if (action.equals("findatm")) {
            AtmListGUI.open(player);
        } else if (action.startsWith("smsto:")) {
            promptSms(player, action.substring("smsto:".length()));
        }
    }

    /** Generic action any app can tag an icon with to run a command as the clicking player. */
    private void runCommand(Player player, String command) {
        player.closeInventory();
        plugin.getServer().dispatchCommand(player, command);
    }

    private void promptSms(Player player, String targetName) {
        player.closeInventory();
        plugin.getPendingSmsService().start(player.getUniqueId(), targetName);
        player.sendMessage("§bType your message to §e" + targetName + " §bin chat (or type 'cancel').");
    }

    private void payBill(Player player) {
        try {
            ServicePlan plan = plugin.getServicePlanService().getPlan(player.getUniqueId());
            if (plugin.getBillingService().charge(player, plan.getMonthlyCost())) {
                plugin.getServicePlanService().setServiceActive(player.getUniqueId(), true);
                plugin.getServicePlanService().setLastBilled(player.getUniqueId(), System.currentTimeMillis());
                player.sendMessage("§aPaid $" + plan.getMonthlyCost() + " - your service is now active.");
            } else {
                player.sendMessage("§cInsufficient funds to pay your bill ($" + plan.getMonthlyCost() + ").");
            }
        } catch (SQLException e) {
            player.sendMessage("§cFailed to pay bill: " + e.getMessage());
        }
        plugin.getPhoneService().openPhone(player);
    }

    private void toggleInstall(Player player, String appId, boolean install) {
        try {
            if (install) {
                plugin.getInstalledAppsStore().install(player.getUniqueId(), appId);
            } else {
                plugin.getInstalledAppsStore().uninstall(player.getUniqueId(), appId);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("GuiClickListener: failed to update installed apps: " + e.getMessage());
        }
        PhoneApp app = plugin.getAppRegistry().getApp("appstore");
        if (app != null) app.open(player);
    }

    private void connectToServer(Player player, String serverName) {
        if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "BungeeCord")) return;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
        } catch (IOException ignored) {
        }
        player.sendPluginMessage(plugin, "BungeeCord", bytes.toByteArray());
    }

    private void removeTower(Player player, String towerName) {
        if (!player.hasPermission("allyphone.celltower")) return;
        try {
            plugin.getCellTowerStore().removeByName(towerName);
            plugin.getTowerMapIntegration().refresh();
            player.sendMessage("§aRemoved cell tower: " + towerName);
        } catch (SQLException e) {
            player.sendMessage("§cFailed to remove tower: " + e.getMessage());
        }
        PhoneApp app = plugin.getAppRegistry().getApp("towers");
        if (app != null) app.open(player);
    }

    private boolean hasService(Player player) {
        try {
            return plugin.getSignalService().hasService(player)
                    && plugin.getServicePlanService().isServiceActive(player.getUniqueId());
        } catch (SQLException e) {
            return true;
        }
    }

    private String reasonFor(Player player) {
        if (!plugin.getSignalService().hasService(player)) return "No signal in this area.";
        return "Your service is suspended. Pay your bill to restore access.";
    }
}
