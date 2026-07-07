package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.AtmListGUI;
import com.allyphone.gui.CreditGUI;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.MessagesGUI;
import com.allyphone.gui.NoServiceGUI;
import com.allyphone.gui.PetsListGUI;
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
        } else if (action.equals("credit")) {
            CreditGUI.open(player);
        } else if (action.equals("loanrequest")) {
            promptAmount(player, "§bType the loan amount to request in chat (or type 'cancel').", "loan request ");
        } else if (action.equals("loanpay")) {
            promptAmount(player, "§bType the amount to pay towards your loan in chat (or type 'cancel').", "loan pay ");
        } else if (action.equals("cardcharge")) {
            promptAmount(player, "§bType the amount to charge to your card in chat (or type 'cancel').", "creditcard charge ");
        } else if (action.equals("cardpay")) {
            promptAmount(player, "§bType the amount to pay towards your card in chat (or type 'cancel').", "creditcard pay ");
        } else if (action.equals("mypets")) {
            PetsListGUI.open(player);
        } else if (action.equals("8ball")) {
            com.allyphone.apps.EightBallApp.shake(player);
        } else if (action.startsWith("arcade:")) {
            com.allyphone.apps.ArcadeApp.play(player, action.substring("arcade:".length()));
        } else if (action.startsWith("casino:")) {
            com.allyphone.apps.CasinoApp.play(player, action.substring("casino:".length()));
        } else if (action.equals("togglecoverage")) {
            toggleCoverage(player);
        } else if (action.startsWith("smsto:")) {
            promptSms(player, action.substring("smsto:".length()));
        } else if (action.startsWith("settheme:")) {
            setTheme(player, action.substring("settheme:".length()));
        } else if (action.equals("renamephone")) {
            promptRename(player);
        } else if (action.startsWith("reorder:")) {
            reorderApp(player, action.substring("reorder:".length()));
        } else if (action.startsWith("deletemsg:")) {
            if (shiftClick) {
                deleteMessage(player, action.substring("deletemsg:".length()));
            }
        }
    }

    private void deleteMessage(Player player, String idText) {
        try {
            long id = Long.parseLong(idText);
            plugin.getMessageSqlService().delete(id, player.getUniqueId());
        } catch (NumberFormatException | SQLException e) {
            player.sendMessage("§cFailed to delete message.");
        }
        MessagesGUI.open(player);
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

    private void setTheme(Player player, String themeName) {
        try {
            com.allyphone.service.PhoneCustomizationStore.Theme theme =
                    com.allyphone.service.PhoneCustomizationStore.Theme.valueOf(themeName);
            plugin.getPhoneCustomizationStore().setTheme(player.getUniqueId(), theme);
        } catch (IllegalArgumentException | SQLException e) {
            player.sendMessage("§cFailed to set theme.");
        }
        PhoneApp app = plugin.getAppRegistry().getApp("customize");
        if (app != null) app.open(player);
    }

    private void promptRename(Player player) {
        player.closeInventory();
        player.sendMessage("§bType the new name for your phone in chat (or type 'cancel').");
        plugin.getPendingInputService().await(player.getUniqueId(), text -> {
            if (!player.isOnline()) return;
            if (text.equalsIgnoreCase("cancel")) {
                player.sendMessage("§7Rename cancelled.");
                return;
            }
            String clean = org.bukkit.ChatColor.stripColor(text).trim();
            if (clean.isEmpty()) {
                player.sendMessage("§cThat name isn't valid.");
                return;
            }
            if (clean.length() > 32) {
                clean = clean.substring(0, 32);
            }
            try {
                plugin.getPhoneCustomizationStore().setNickname(player.getUniqueId(), clean);
                int slot = plugin.getPhoneService().findPhoneSlot(player);
                if (slot >= 0) {
                    player.getInventory().setItem(slot, plugin.getPhoneService().createPhoneItem(player));
                }
                player.sendMessage("§aYour phone is now named: " + clean);
            } catch (SQLException e) {
                player.sendMessage("§cFailed to rename your phone: " + e.getMessage());
            }
        });
    }

    /** Prompts for a positive dollar amount in chat, then dispatches "<commandPrefix><amount>" as the player. */
    private void promptAmount(Player player, String prompt, String commandPrefix) {
        player.closeInventory();
        player.sendMessage(prompt);
        plugin.getPendingInputService().await(player.getUniqueId(), text -> {
            if (!player.isOnline()) return;
            if (text.equalsIgnoreCase("cancel")) {
                player.sendMessage("§7Cancelled.");
                CreditGUI.open(player);
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(text.trim());
            } catch (NumberFormatException e) {
                player.sendMessage("§cThat's not a valid amount.");
                CreditGUI.open(player);
                return;
            }
            if (!Double.isFinite(amount) || amount <= 0) {
                player.sendMessage("§cAmount must be a positive, finite number.");
                CreditGUI.open(player);
                return;
            }
            plugin.getServer().dispatchCommand(player, commandPrefix + amount);
            CreditGUI.open(player);
        });
    }

    private void reorderApp(Player player, String payload) {
        int split = payload.lastIndexOf(':');
        if (split < 0) return;
        String appId = payload.substring(0, split);
        String direction = payload.substring(split + 1);

        try {
            java.util.List<String> order = new java.util.ArrayList<>(
                    plugin.getPhoneCustomizationStore().getAppOrder(player.getUniqueId()));
            java.util.Set<String> installed = plugin.getInstalledAppsStore().getInstalled(player.getUniqueId());
            order.removeIf(id -> !installed.contains(id) || id.equals("appstore"));
            for (PhoneApp app : plugin.getAppRegistry().getAllApps()) {
                String id = app.getId();
                if (!id.equals("appstore") && installed.contains(id) && !order.contains(id)) {
                    order.add(id);
                }
            }

            int index = order.indexOf(appId);
            int swapWith = direction.equals("up") ? index - 1 : index + 1;
            if (index >= 0 && swapWith >= 0 && swapWith < order.size()) {
                java.util.Collections.swap(order, index, swapWith);
                plugin.getPhoneCustomizationStore().setAppOrder(player.getUniqueId(), order);
            }
        } catch (SQLException e) {
            player.sendMessage("§cFailed to reorder apps: " + e.getMessage());
        }
        PhoneApp app = plugin.getAppRegistry().getApp("customize");
        if (app != null) app.open(player);
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
        PhoneApp target = plugin.getAppRegistry().getApp(appId);
        if (!install && target != null && target.isEssential()) {
            player.sendMessage("§c" + target.getDisplayName() + " §7is a core app and cannot be uninstalled.");
            PhoneApp store = plugin.getAppRegistry().getApp("appstore");
            if (store != null) store.open(player);
            return;
        }
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

    private void toggleCoverage(Player player) {
        boolean nowOn = plugin.getCellTowerVisualizer().toggle(player);
        player.sendMessage(nowOn
                ? "§aShowing cell coverage rings around you."
                : "§7Coverage overlay hidden.");
        PhoneApp app = plugin.getAppRegistry().getApp("towers");
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
