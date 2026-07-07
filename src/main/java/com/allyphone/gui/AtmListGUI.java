package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.service.AtmStore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/** Lists registered ATMs sorted by distance; physical cash withdrawal itself happens via AlsBanker. */
public class AtmListGUI {

    private AtmListGUI() {
    }

    public static void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();

        List<AtmStore.Atm> atms;
        try {
            atms = plugin.getAtmStore().getAll();
        } catch (SQLException e) {
            atms = List.of();
        }

        atms.sort(Comparator.comparingDouble(atm -> {
            double dist = distance(player, atm);
            return dist < 0 ? Double.MAX_VALUE : dist;
        }));

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§2§lNearby ATMs");
        holder.setInventory(inv);

        if (atms.isEmpty()) {
            inv.setItem(13, GuiUtil.icon("no_atms_registered", "§7No ATMs registered",
                    "§7Ask a staff member to add one", "§7with /atm add <name>"));
        } else {
            int slot = 0;
            for (AtmStore.Atm atm : atms) {
                if (slot >= 18) break;
                double dist = distance(player, atm);
                inv.setItem(slot++, GuiUtil.icon("emerald_block", "§a" + atm.name(),
                        "§7World: " + atm.world(),
                        "§7Coords: " + atm.x() + ", " + atm.y() + ", " + atm.z(),
                        dist >= 0 ? "§7Distance: " + (int) dist + " blocks" : "§7(different world)"));
            }
        }

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.APP_ID_KEY, "wallet"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }

    private static double distance(Player player, AtmStore.Atm atm) {
        if (!atm.world().equals(player.getWorld().getName())) return -1;
        double dx = atm.x() - player.getLocation().getX();
        double dy = atm.y() - player.getLocation().getY();
        double dz = atm.z() - player.getLocation().getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
