package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ExtrasApp implements PhoneApp {

    @Override
    public String getId() {
        return "extras";
    }

    @Override
    public String getDisplayName() {
        return "§7✨ Extras";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("misc_tools_info", getDisplayName(), "§7Misc tools & info");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§7§lExtras");
        holder.setInventory(inv);

        inv.setItem(10, GuiUtil.icon("allyphone_v", "§fAllyPhone v" + plugin.getDescription().getVersion(),
                "§7By " + plugin.getDescription().getAuthors()));
        inv.setItem(12, GuiUtil.icon("server_time", "§fServer Time",
                "§7" + player.getWorld().getTime() + " ticks"));
        inv.setItem(14, GuiUtil.icon("your_location", "§fYour Location",
                "§7X: " + player.getLocation().getBlockX(),
                "§7Y: " + player.getLocation().getBlockY(),
                "§7Z: " + player.getLocation().getBlockZ()));

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }
}
