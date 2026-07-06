package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.gui.PhoneHomeGUI;
import com.allyphone.item.PhoneItem;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class PhoneService {

    public ItemStack createPhoneItem() {
        return PhoneItem.create(AllyPhonePlugin.get());
    }

    public ItemStack createPhoneItem(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        try {
            String nickname = plugin.getPhoneCustomizationStore().getNickname(player.getUniqueId());
            return PhoneItem.create(plugin, nickname);
        } catch (SQLException e) {
            return PhoneItem.create(plugin);
        }
    }

    public boolean isPhone(ItemStack item) {
        return PhoneItem.isPhone(AllyPhonePlugin.get(), item);
    }

    public boolean hasPhone(Player player) {
        return findPhoneSlot(player) >= 0;
    }

    /** Inventory slot index of the player's phone, or -1 if they don't have one. */
    public int findPhoneSlot(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isPhone(contents[i])) return i;
        }
        return -1;
    }

    public void deliverPhone(Player player) {
        if (!hasPhone(player)) {
            ItemStack phone = createPhoneItem(player);

            if (player.getInventory().getItem(8) == null) {
                player.getInventory().setItem(8, phone);
            } else {
                player.getInventory().addItem(phone);
            }

            player.sendActionBar("§a📱 Your AllyPhone has been delivered!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
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
