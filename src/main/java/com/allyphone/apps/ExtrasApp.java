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
        return GuiUtil.icon(Material.CHEST_MINECART, getDisplayName(), "§7Misc tools & info");
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

        inv.setItem(10, GuiUtil.icon(Material.NAME_TAG, "§fAllyPhone v" + plugin.getDescription().getVersion(),
                "§7By " + plugin.getDescription().getAuthors()));
        inv.setItem(12, GuiUtil.icon(Material.CLOCK, "§fServer Time",
                "§7" + player.getWorld().getTime() + " ticks"));
        inv.setItem(14, GuiUtil.icon(Material.COMPASS, "§fYour Location",
                "§7X: " + player.getLocation().getBlockX(),
                "§7Y: " + player.getLocation().getBlockY(),
                "§7Z: " + player.getLocation().getBlockZ()));

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
