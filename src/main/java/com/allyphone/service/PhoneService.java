package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.gui.PhoneHomeGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PhoneService {

    public ItemStack createPhoneItem() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();

        meta.setDisplayName("§f§lAllyPhone 67 Pro Max");
        meta.setLore(java.util.Arrays.asList(
                "§7Right-click to open",
                "§7Your link to the city",
                "§8§rcityphone-item"
        ));

        head.setItemMeta(meta);
        return head;
    }

    public boolean isPhone(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String name = meta.getDisplayName();
        if (name != null && name.contains("AllyPhone")) return true;

        if (meta.getLore() != null) {
            for (String line : meta.getLore()) {
                if (line.contains("cityphone-item")) return true;
            }
        }

        return false;
    }

    public boolean hasPhone(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isPhone(item)) return true;
        }
        return false;
    }

    public void deliverPhone(Player player) {
        if (!hasPhone(player)) {
            ItemStack phone = createPhoneItem();

            if (player.getInventory().getItem(8) == null) {
                player.getInventory().setItem(8, phone);
            } else {
                player.getInventory().addItem(phone);
            }

            player.sendActionBar("§a📱 Your AllyPhone has been delivered!");
            player.playSound(player.getLocation(), "block.note_block.pling", 1, 2);
        }
    }

    public void register(Player player) {
        deliverPhone(player);
    }

    public void openPhone(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        new PhoneHomeGUI(this, plugin.getSignalService(), plugin.getBillingService()).open(player);
    }
}
