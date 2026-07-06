package com.allyphone.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds and identifies the AllyPhone item. Identification uses a
 * PersistentDataContainer tag so renaming/reskinning the item cannot break or fake it;
 * the legacy display-name/lore check is kept as a fallback for phones handed
 * out by older (player-head) versions of this plugin.
 */
public final class PhoneItem {

    private static final String KEY_NAME = "phone";
    private static final int CUSTOM_MODEL_DATA = 1;

    private PhoneItem() {
    }

    public static ItemStack create(Plugin plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§f§lAllyPhone 67 Pro Max");
        meta.setLore(baseLore());
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isPhone(Plugin plugin, ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        if (meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.BYTE)) return true;

        // Legacy fallback for phones created before the PDC tag existed (old player-head phones)
        if (item.getType() == Material.PLAYER_HEAD) {
            String name = meta.getDisplayName();
            if (name != null && name.contains("AllyPhone")) return true;
            if (meta.getLore() != null) {
                for (String line : meta.getLore()) {
                    if (line.contains("cityphone-item")) return true;
                }
            }
        }
        return false;
    }

    /** Refreshes the phone's lore/glow to reflect the player's unread notification count. */
    public static ItemStack applyUnreadBadge(Plugin plugin, ItemStack item, int unreadCount) {
        if (!isPhone(plugin, item)) return item;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(baseLore());
        if (unreadCount > 0) {
            lore.add("§c§l● " + unreadCount + " new message" + (unreadCount == 1 ? "" : "s"));
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(Enchantment.DURABILITY);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> baseLore() {
        return new ArrayList<>(Arrays.asList(
                "§7Right-click to open",
                "§7Your link to the city",
                "§8§rcityphone-item"
        ));
    }

    private static NamespacedKey key(Plugin plugin) {
        return new NamespacedKey(plugin, KEY_NAME);
    }
}
