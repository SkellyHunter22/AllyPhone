package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Set;

public class AppStoreApp implements PhoneApp {

    @Override
    public String getId() {
        return "appstore";
    }

    @Override
    public String getDisplayName() {
        return "§9🏪 App Store";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.CHEST, getDisplayName(), "§7Install & remove apps");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();

        Set<String> installed;
        try {
            installed = plugin.getInstalledAppsStore().getInstalled(player.getUniqueId());
        } catch (SQLException e) {
            installed = Set.of();
        }

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 45, "§9§lApp Store");
        holder.setInventory(inv);

        int slot = 0;
        for (PhoneApp app : plugin.getAppRegistry().getAllApps()) {
            if (app.getId().equals("appstore") || slot >= 36) continue;

            boolean isInstalled = installed.contains(app.getId().toLowerCase());
            ItemStack icon = GuiUtil.icon(isInstalled ? Material.LIME_WOOL : Material.RED_WOOL,
                    app.getDisplayName(),
                    isInstalled ? "§aInstalled" : "§7Not installed",
                    isInstalled ? "§eClick to uninstall" : "§eClick to install");
            icon = GuiUtil.tagged(plugin, icon, GuiUtil.ACTION_KEY,
                    (isInstalled ? "uninstall:" : "install:") + app.getId());
            inv.setItem(slot++, icon);
        }

        inv.setItem(44, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
