package com.allyphone.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/** Small helper for building the icon ItemStacks used across AllyPhone GUIs. */
public final class GuiUtil {

    public static final String APP_ID_KEY = "app_id";
    public static final String ACTION_KEY = "action";

    private GuiUtil() {
    }

    public static ItemStack icon(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack tagged(Plugin plugin, ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    public static String getTag(Plugin plugin, ItemStack item, String key) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }

    public static ItemStack backButton() {
        return icon(Material.ARROW, "§7« Back", "§8Return to home screen");
    }

    /** Dark bezel filler used to frame GUIs so they read as a phone screen rather than a chest. */
    public static ItemStack filler() {
        return icon(Material.BLACK_STAINED_GLASS_PANE, " ");
    }

    /** Fills every empty slot in the top and bottom row of an inventory with bezel panes. */
    public static void addBezel(Inventory inv) {
        int size = inv.getSize();
        for (int i = 0; i < 9 && i < size; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, filler());
        }
        for (int i = size - 9; i < size; i++) {
            if (i >= 0 && inv.getItem(i) == null) inv.setItem(i, filler());
        }
    }
}
