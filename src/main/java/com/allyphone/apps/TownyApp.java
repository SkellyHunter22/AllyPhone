package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import com.allyphone.integration.TownyPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TownyApp implements PhoneApp {

    @Override
    public String getId() {
        return "towny";
    }

    @Override
    public String getDisplayName() {
        return "§2🏘 Towny";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.OAK_DOOR, getDisplayName(), "§7Town & nation info");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        boolean present = Bukkit.getPluginManager().isPluginEnabled("Towny");

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§2§lTowny");
        holder.setInventory(inv);

        if (!present) {
            inv.setItem(13, GuiUtil.icon(Material.BARRIER, "§cTowny is not installed"));
            inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
            GuiUtil.addBezel(inv);
            player.openInventory(inv);
            return;
        }

        String townInfo = TownyPlaceholders.resolve(player, "%towny_resident_town%", "no town");
        String nationInfo = TownyPlaceholders.resolve(player, "%towny_resident_nation%", "no nation");
        inv.setItem(4, GuiUtil.icon(Material.OAK_DOOR, "§aYour Town: §f" + townInfo,
                "§7Nation: §f" + nationInfo));

        inv.setItem(10, GuiUtil.tagged(plugin,
                GuiUtil.icon(Material.MAP, "§eTown Info", "§7/town"),
                GuiUtil.ACTION_KEY, "command:town"));
        inv.setItem(11, GuiUtil.tagged(plugin,
                GuiUtil.icon(Material.ENDER_PEARL, "§eTeleport to Town Spawn", "§7/town spawn"),
                GuiUtil.ACTION_KEY, "command:town spawn"));
        inv.setItem(12, GuiUtil.tagged(plugin,
                GuiUtil.icon(Material.PLAYER_HEAD, "§eResident Info", "§7/resident"),
                GuiUtil.ACTION_KEY, "command:resident"));
        inv.setItem(13, GuiUtil.tagged(plugin,
                GuiUtil.icon(Material.GOLDEN_APPLE, "§eNation Info", "§7/nation"),
                GuiUtil.ACTION_KEY, "command:nation"));
        inv.setItem(14, GuiUtil.tagged(plugin,
                GuiUtil.icon(Material.EMERALD, "§eTown Bank", "§7/town deposit <amount>"),
                GuiUtil.ACTION_KEY, "command:town"));
        inv.setItem(15, GuiUtil.tagged(plugin,
                GuiUtil.icon(Material.IRON_SWORD, "§eSpawn to Nation", "§7/nation spawn"),
                GuiUtil.ACTION_KEY, "command:nation spawn"));

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
