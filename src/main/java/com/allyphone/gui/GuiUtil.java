package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.service.PhoneCustomizationStore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
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

    /**
     * Same as {@link #icon(Material, String, String...)} but rendered with one of AllyPhone's own
     * custom icons instead of a vanilla item texture. {@code customIconKey} must match a model at
     * assets/allyphone/models/item/icons/&lt;key&gt;.json in the bundled resource pack.
     */
    public static ItemStack icon(String customIconKey, String name, String... lore) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        meta.setItemModel(new NamespacedKey(AllyPhonePlugin.get(), "icons/" + customIconKey));
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
        return icon("back", "§7« Back", "§8Return to home screen");
    }

    /** Dark bezel filler used to frame GUIs so they read as a phone screen rather than a chest. */
    public static ItemStack filler() {
        return icon(Material.BLACK_STAINED_GLASS_PANE, " ");
    }

    /** Bezel filler in a player-chosen theme material, falling back to the default black pane. */
    public static ItemStack filler(Material material) {
        return icon(material != null ? material : Material.BLACK_STAINED_GLASS_PANE, " ");
    }

    /** Fills every empty slot in the top and bottom row of an inventory with bezel panes. */
    public static void addBezel(Inventory inv) {
        addBezel(inv, null);
    }

    /** Same as {@link #addBezel(Inventory)} but using the given player's chosen bezel theme, if any. */
    public static void addThemedBezel(Inventory inv, Player player) {
        Material themeMaterial = null;
        try {
            PhoneCustomizationStore.Theme theme = AllyPhonePlugin.get().getPhoneCustomizationStore().getTheme(player.getUniqueId());
            themeMaterial = Material.matchMaterial(theme.materialName);
        } catch (SQLException ignored) {
        }
        addBezel(inv, themeMaterial);
    }

    /** Same as {@link #addBezel(Inventory)} but using the given theme material for the panes. */
    public static void addBezel(Inventory inv, Material themeMaterial) {
        int size = inv.getSize();
        for (int i = 0; i < 9 && i < size; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, filler(themeMaterial));
        }
        for (int i = size - 9; i < size; i++) {
            if (i >= 0 && inv.getItem(i) == null) inv.setItem(i, filler(themeMaterial));
        }
    }
}
