package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.service.BillingService;
import com.allyphone.service.PhoneCustomizationStore;
import com.allyphone.service.PhoneService;
import com.allyphone.service.SignalService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PhoneHomeGUI {

    private final PhoneService phoneService;
    private final SignalService signalService;
    private final BillingService billingService;

    public PhoneHomeGUI(PhoneService phoneService,
                        SignalService signalService,
                        BillingService billingService) {
        this.phoneService = phoneService;
        this.signalService = signalService;
        this.billingService = billingService;
    }

    // Row 0 = status bar, rows 1-4 = a 7-wide app grid bordered by filler, row 5 = dock.
    private static final int[] GRID_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43,
    };

    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();

        Set<String> installed;
        try {
            installed = plugin.getInstalledAppsStore().getInstalled(player.getUniqueId());
        } catch (SQLException e) {
            plugin.getLogger().warning("PhoneHomeGUI: failed to load installed apps: " + e.getMessage());
            installed = Set.of();
        }

        boolean serviceActive;
        try {
            serviceActive = plugin.getServicePlanService().isServiceActive(player.getUniqueId());
        } catch (SQLException e) {
            serviceActive = true;
        }

        PhoneCustomizationStore.Theme theme;
        try {
            theme = plugin.getPhoneCustomizationStore().getTheme(player.getUniqueId());
        } catch (SQLException e) {
            theme = PhoneCustomizationStore.Theme.BLACK;
        }
        Material themeMaterial = Material.matchMaterial(theme.materialName);

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§0§l📱 AllyPhone");
        holder.setInventory(inv);

        // Themed bezel: top status bar, side edges around the app grid, and bottom dock.
        ItemStack bezel = GuiUtil.filler(themeMaterial);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, bezel);
        }
        for (int i : new int[]{9, 17, 18, 26, 27, 35, 36, 44}) {
            inv.setItem(i, bezel);
        }
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, bezel);
        }

        int signal = signalService.getSignalStrength(player);
        inv.setItem(1, GuiUtil.icon(signal > 0 ? Material.LIME_DYE : Material.RED_DYE,
                signal > 0 ? "§a§l📶 Signal: " + signal + "/5" : "§c§l📶 No Signal"));

        int unread = plugin.getMessageService().getUnreadCount(player.getUniqueId());
        inv.setItem(4, GuiUtil.icon(unread > 0 ? Material.WRITABLE_BOOK : Material.PAPER,
                unread > 0 ? "§e§l✉ " + unread + " new message" + (unread == 1 ? "" : "s") : "§7✉ No new messages"));

        inv.setItem(7, GuiUtil.icon(serviceActive ? Material.EMERALD : Material.BARRIER,
                serviceActive ? "§a§l● Service Active" : "§c§l● Service Suspended",
                serviceActive ? "" : "§7Click Pay Bill in Wallet to restore service."));

        List<String> order = resolveOrder(plugin, player, installed);
        int slot = 0;
        for (String appId : order) {
            if (slot >= GRID_SLOTS.length) break;
            PhoneApp app = plugin.getAppRegistry().getApp(appId);
            if (app == null) continue;
            if (app.requiredPermission() != null && !player.hasPermission(app.requiredPermission())) continue;

            ItemStack icon = GuiUtil.tagged(plugin, app.getIcon(player), GuiUtil.APP_ID_KEY, app.getId());
            inv.setItem(GRID_SLOTS[slot++], icon);
        }

        // Home bar / dock, centered like a phone's bottom dock.
        inv.setItem(48, GuiUtil.icon(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(49, GuiUtil.tagged(plugin,
                GuiUtil.icon(Material.CHEST, "§9🏪 App Store", "§7Install & remove apps"),
                GuiUtil.APP_ID_KEY, "appstore"));
        inv.setItem(50, GuiUtil.icon(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));

        player.openInventory(inv);
    }

    /** Installed apps (minus appstore) in the player's saved order, appending any not yet ordered. */
    private List<String> resolveOrder(AllyPhonePlugin plugin, Player player, Set<String> installed) {
        List<String> order;
        try {
            order = new ArrayList<>(plugin.getPhoneCustomizationStore().getAppOrder(player.getUniqueId()));
        } catch (SQLException e) {
            order = new ArrayList<>();
        }
        order.removeIf(id -> !installed.contains(id) || id.equals("appstore"));
        for (PhoneApp app : plugin.getAppRegistry().getAllApps()) {
            String id = app.getId();
            if (id.equals("appstore")) continue;
            if (installed.contains(id) && !order.contains(id)) {
                order.add(id);
            }
        }
        return order;
    }
}
