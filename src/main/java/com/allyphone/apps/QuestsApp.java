package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Bridges to Quests (me.pikamug.quests) - opens its own GUI via /quests. */
public class QuestsApp implements PhoneApp {

    @Override
    public String getId() {
        return "quests";
    }

    @Override
    public String getDisplayName() {
        return "§b📜 Quests";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("track_your_quests", getDisplayName(), "§7Track your quests");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        boolean present = Bukkit.getPluginManager().isPluginEnabled("Quests");

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§b§lQuests");
        holder.setInventory(inv);

        if (present) {
            inv.setItem(13, GuiUtil.tagged(plugin,
                    GuiUtil.icon("open_quests_menu", "§eOpen Quests Menu", "§7Click to open Quests"),
                    GuiUtil.ACTION_KEY, "command:quests"));
        } else {
            inv.setItem(13, GuiUtil.icon("quests_is_not_installed", "§cQuests is not installed"));
        }

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }
}
