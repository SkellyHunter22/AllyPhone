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

/**
 * Bridges to EcoJobs. Its Kotlin API isn't safe to compile against directly without
 * source access, so this just opens EcoJobs' own GUI via its /jobs command.
 */
public class JobsApp implements PhoneApp {

    @Override
    public String getId() {
        return "jobs";
    }

    @Override
    public String getDisplayName() {
        return "§6⚒ Jobs";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.IRON_PICKAXE, getDisplayName(), "§7Manage your jobs");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        boolean present = Bukkit.getPluginManager().isPluginEnabled("EcoJobs");

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§6§lJobs");
        holder.setInventory(inv);

        if (present) {
            inv.setItem(13, GuiUtil.tagged(plugin,
                    GuiUtil.icon(Material.IRON_PICKAXE, "§eOpen Jobs Menu", "§7Click to open EcoJobs"),
                    GuiUtil.ACTION_KEY, "command:jobs"));
        } else {
            inv.setItem(13, GuiUtil.icon(Material.BARRIER, "§cEcoJobs is not installed"));
        }

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
