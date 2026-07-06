package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import com.allyphone.service.CellTowerStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;

public class TowerManagerApp implements PhoneApp {

    @Override
    public String getId() {
        return "towers";
    }

    @Override
    public String getDisplayName() {
        return "§b📡 Cell Towers";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.LIGHTNING_ROD, getDisplayName(), "§7Coverage map");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();

        List<CellTowerStore.CellTower> towers;
        try {
            towers = plugin.getCellTowerStore().getAll();
        } catch (SQLException e) {
            towers = List.of();
        }

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§b§lCell Towers");
        holder.setInventory(inv);

        boolean showingCoverage = plugin.getCellTowerVisualizer().isActive(player);
        inv.setItem(48, GuiUtil.tagged(plugin,
                GuiUtil.icon(showingCoverage ? Material.ENDER_EYE : Material.ENDER_PEARL,
                        showingCoverage ? "§aCoverage Overlay: ON" : "§7Coverage Overlay: OFF",
                        "§7Click to " + (showingCoverage ? "hide" : "show") + " reception rings around you"),
                GuiUtil.ACTION_KEY, "togglecoverage"));

        int slot = 0;
        if (towers.isEmpty()) {
            inv.setItem(4, GuiUtil.icon(Material.BARRIER, "§7No cell towers registered",
                    "§7Use §f/celltower add <name> <radius>"));
        } else {
            boolean canRemove = player.hasPermission("allyphone.celltower");
            for (CellTowerStore.CellTower tower : towers) {
                if (slot >= 45) break;
                double dist = tower.world().equals(player.getWorld().getName())
                        ? player.getLocation().distance(new Location(player.getWorld(), tower.x(), tower.y(), tower.z()))
                        : -1;
                ItemStack icon = GuiUtil.icon(Material.LIGHTNING_ROD, "§e" + tower.name(),
                        "§7World: " + tower.world(),
                        "§7Radius: " + tower.radius(),
                        dist >= 0 ? "§7Distance: " + (int) dist + " blocks" : "§7(different world)",
                        canRemove ? "§cShift-click to remove" : "");
                if (canRemove) {
                    icon = GuiUtil.tagged(plugin, icon, GuiUtil.ACTION_KEY, "removetower:" + tower.name());
                }
                inv.setItem(slot++, icon);
            }
        }

        inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
