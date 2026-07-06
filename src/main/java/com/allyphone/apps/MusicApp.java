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

/** Bridges to GMusic (dev.geco.gmusic) via its /gmusic command. */
public class MusicApp implements PhoneApp {

    @Override
    public String getId() {
        return "music";
    }

    @Override
    public String getDisplayName() {
        return "§5🎵 Music";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.MUSIC_DISC_CAT, getDisplayName(), "§7Play music");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        boolean present = Bukkit.getPluginManager().isPluginEnabled("GMusic");

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§5§lMusic");
        holder.setInventory(inv);

        if (present) {
            inv.setItem(10, GuiUtil.tagged(plugin,
                    GuiUtil.icon(Material.MUSIC_DISC_CAT, "§ePlay Random", "§7/gmusic random"),
                    GuiUtil.ACTION_KEY, "command:gmusic random"));
            inv.setItem(11, GuiUtil.tagged(plugin,
                    GuiUtil.icon(Material.PAPER, "§eNow Playing", "§7/gmusic playing"),
                    GuiUtil.ACTION_KEY, "command:gmusic playing"));
            inv.setItem(12, GuiUtil.tagged(plugin,
                    GuiUtil.icon(Material.REDSTONE_TORCH, "§eToggle", "§7/gmusic toggle"),
                    GuiUtil.ACTION_KEY, "command:gmusic toggle"));
            inv.setItem(13, GuiUtil.tagged(plugin,
                    GuiUtil.icon(Material.ARROW, "§eSkip", "§7/gmusic skip"),
                    GuiUtil.ACTION_KEY, "command:gmusic skip"));
            inv.setItem(14, GuiUtil.tagged(plugin,
                    GuiUtil.icon(Material.BARRIER, "§eStop", "§7/gmusic stop"),
                    GuiUtil.ACTION_KEY, "command:gmusic stop"));
        } else {
            inv.setItem(13, GuiUtil.icon(Material.BARRIER, "§cGMusic is not installed"));
        }

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
