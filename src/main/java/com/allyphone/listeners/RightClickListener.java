package com.allyphone.listeners;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.item.PhoneItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class RightClickListener implements Listener {

    private final AllyPhonePlugin plugin;

    public RightClickListener(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!PhoneItem.isPhone(plugin, item)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);
        plugin.getPhoneService().openPhone(player);
    }
}
