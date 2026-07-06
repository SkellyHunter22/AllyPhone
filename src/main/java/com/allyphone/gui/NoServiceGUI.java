package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class NoServiceGUI {

    private NoServiceGUI() {
    }

    public static void open(Player player, String reason) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 9, "§4No Service");
        holder.setInventory(inv);

        inv.setItem(4, GuiUtil.icon(Material.BARRIER, "§c§lNo Service", "§7" + reason));
        inv.setItem(8, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));

        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
