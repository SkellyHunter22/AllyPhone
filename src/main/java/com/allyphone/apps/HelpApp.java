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

public class HelpApp implements PhoneApp {

    @Override
    public String getId() {
        return "help";
    }

    @Override
    public String getDisplayName() {
        return "§f❓ Help";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.KNOWLEDGE_BOOK, getDisplayName(), "§7Commands & tips");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§f§lHelp");
        holder.setInventory(inv);

        inv.setItem(10, GuiUtil.icon(Material.PAPER, "§e/phone", "§7Open your AllyPhone"));
        inv.setItem(11, GuiUtil.icon(Material.PAPER, "§e/sms <player> <message>", "§7Send a text message"));
        inv.setItem(12, GuiUtil.icon(Material.PAPER, "§e/phonenews <title> | <body>", "§7Post city news (staff)"));
        inv.setItem(13, GuiUtil.icon(Material.PAPER, "§e/celltower add|remove|list", "§7Manage cell towers (staff)"));
        inv.setItem(14, GuiUtil.icon(Material.PAPER, "§7Right-click your phone", "§7to open it anytime"));

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
