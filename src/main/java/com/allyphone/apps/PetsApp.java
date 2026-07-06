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

/** Bridges to GPet (dev.geco.gpet) - opens its own buy/manage GUI via /gpet gui. */
public class PetsApp implements PhoneApp {

    @Override
    public String getId() {
        return "pets";
    }

    @Override
    public String getDisplayName() {
        return "§d🐾 Pets";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.BONE, getDisplayName(), "§7Buy & manage pets");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        boolean present = Bukkit.getPluginManager().isPluginEnabled("GPet");

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§d§lPets");
        holder.setInventory(inv);

        if (present) {
            inv.setItem(13, GuiUtil.tagged(plugin,
                    GuiUtil.icon(Material.BONE, "§eBuy & Manage Pets", "§7Click to open GPet"),
                    GuiUtil.ACTION_KEY, "command:gpet gui"));
        } else {
            inv.setItem(13, GuiUtil.icon(Material.BARRIER, "§cGPet is not installed"));
        }

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
