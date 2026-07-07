package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import com.allyphone.service.PhoneCustomizationStore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/** Lets players pick a bezel theme, rename their phone, and reorder their home-screen apps. */
public class CustomizeApp implements PhoneApp {

    private static final int[] APP_SLOTS = {19, 20, 21, 22, 23, 24, 25};
    private static final int[] UP_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private static final int[] DOWN_SLOTS = {37, 38, 39, 40, 41, 42, 43};

    @Override
    public String getId() {
        return "customize";
    }

    @Override
    public String getDisplayName() {
        return "§b🎨 Customize";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("theme_phone_name_app_order", getDisplayName(), "§7Theme, phone name & app order");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public boolean isEssential() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneCustomizationStore store = plugin.getPhoneCustomizationStore();

        PhoneCustomizationStore.Theme currentTheme;
        try {
            currentTheme = store.getTheme(player.getUniqueId());
        } catch (SQLException e) {
            currentTheme = PhoneCustomizationStore.Theme.BLACK;
        }

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§b§lCustomize Phone");
        holder.setInventory(inv);

        inv.setItem(4, GuiUtil.icon("bezel_theme", "§b§lBezel Theme", "§7Click a color to apply it"));
        PhoneCustomizationStore.Theme[] themes = PhoneCustomizationStore.Theme.values();
        int[] themeSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < themes.length && i < themeSlots.length; i++) {
            PhoneCustomizationStore.Theme theme = themes[i];
            Material material = Material.matchMaterial(theme.materialName);
            boolean selected = theme == currentTheme;
            ItemStack icon = GuiUtil.icon(material != null ? material : Material.BLACK_STAINED_GLASS_PANE,
                    theme.label + (selected ? " §a(selected)" : ""));
            inv.setItem(themeSlots[i], GuiUtil.tagged(plugin, icon, GuiUtil.ACTION_KEY, "settheme:" + theme.name()));
        }

        inv.setItem(8, GuiUtil.tagged(plugin,
                GuiUtil.icon("rename_phone", "§e§lRename Phone", "§7Click, then type a new name in chat"),
                GuiUtil.ACTION_KEY, "renamephone"));

        inv.setItem(18, GuiUtil.icon("home_screen_order", "§b§lHome Screen Order",
                "§7Use the arrows below to move an app", "§7left/earlier in your app grid"));

        List<String> order = resolveOrder(plugin, player);
        for (int i = 0; i < APP_SLOTS.length; i++) {
            if (i >= order.size()) break;
            String appId = order.get(i);
            PhoneApp app = plugin.getAppRegistry().getApp(appId);
            if (app == null) continue;

            inv.setItem(APP_SLOTS[i], app.getIcon(player));
            if (i > 0) {
                inv.setItem(UP_SLOTS[i], GuiUtil.tagged(plugin,
                        GuiUtil.icon("move_up", "§a▲ Move Up"),
                        GuiUtil.ACTION_KEY, "reorder:" + appId + ":up"));
            }
            if (i < order.size() - 1) {
                inv.setItem(DOWN_SLOTS[i], GuiUtil.tagged(plugin,
                        GuiUtil.icon("move_down", "§c▼ Move Down"),
                        GuiUtil.ACTION_KEY, "reorder:" + appId + ":down"));
            }
        }

        inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv, Material.matchMaterial(currentTheme.materialName));
        player.openInventory(inv);
    }

    /** The player's saved order, extended with any newly-installed apps that aren't in it yet. */
    private List<String> resolveOrder(AllyPhonePlugin plugin, Player player) {
        try {
            Set<String> installed = plugin.getInstalledAppsStore().getInstalled(player.getUniqueId());
            List<String> order = plugin.getPhoneCustomizationStore().getAppOrder(player.getUniqueId());
            order.removeIf(id -> !installed.contains(id) || id.equals("appstore"));
            for (PhoneApp app : plugin.getAppRegistry().getAllApps()) {
                String id = app.getId();
                if (id.equals("appstore")) continue;
                if (installed.contains(id) && !order.contains(id)) {
                    order.add(id);
                }
            }
            return order;
        } catch (SQLException e) {
            return List.of();
        }
    }
}
