package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.item.PhoneItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.sql.SQLException;

/** Registers a cell tower when an authorized player sneak-right-clicks a Beacon while holding their phone. */
public class TowerPatternListener implements Listener {

    private final AllyPhonePlugin plugin;

    public TowerPatternListener(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BEACON) return;
        if (!player.isSneaking() || !player.hasPermission("allyphone.celltower")) return;
        if (!PhoneItem.isPhone(plugin, player.getInventory().getItemInMainHand())) return;

        event.setCancelled(true);
        String name = "tower-" + block.getX() + "-" + block.getY() + "-" + block.getZ();
        int radius = plugin.getConfig().getInt("phone.signal-radius", 500);
        try {
            plugin.getCellTowerStore().add(name, block.getLocation(), radius, player.getUniqueId());
            plugin.getTowerMapIntegration().refresh();
            player.sendMessage("§aCell tower registered: §e" + name + " §7(radius " + radius + ")");
        } catch (SQLException e) {
            player.sendMessage("§cFailed to register cell tower: " + e.getMessage());
        }
    }
}
