package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Admin-only phone menu: one-click buttons for this plugin's own admin commands plus
 * a handful of common server-admin actions, so ops don't have to remember/type them.
 */
public class AdminApp implements PhoneApp {

    public static final String PERMISSION = "allyphone.admin";

    @Override
    public String getId() {
        return "admin";
    }

    @Override
    public String getDisplayName() {
        return "§c🛠 Admin";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.COMMAND_BLOCK, getDisplayName(), "§7Admin tools & commands");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public String requiredPermission() {
        return PERMISSION;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§c§lAdmin Tools");
        holder.setInventory(inv);

        // AllyPhone admin commands. The "add" buttons run the bare command so the plugin's
        // own usage message reminds the admin of the required arguments in chat.
        inv.setItem(10, cmdIcon(plugin, Material.PAPER, "§ePost City News", "§7/phonenews <title> | <body>", "command:phonenews"));
        inv.setItem(11, cmdIcon(plugin, Material.LODESTONE, "§eList Cell Towers", "§7/celltower list", "command:celltower list"));
        inv.setItem(12, cmdIcon(plugin, Material.EMERALD_BLOCK, "§eList ATMs", "§7/atm list", "command:atm list"));
        inv.setItem(13, cmdIcon(plugin, Material.REDSTONE, "§eAdd Cell Tower Here", "§7/celltower add <name> <radius>", "command:celltower add"));
        inv.setItem(14, cmdIcon(plugin, Material.GOLD_INGOT, "§eAdd ATM Here", "§7/atm add <name>", "command:atm add"));

        // General server admin
        inv.setItem(28, cmdIcon(plugin, Material.CLOCK, "§bReload Plugins", "§7/reload confirm", "command:reload confirm"));
        inv.setItem(29, cmdIcon(plugin, Material.BOOK, "§bPlugin List", "§7/plugins", "command:plugins"));
        inv.setItem(30, cmdIcon(plugin, Material.ENDER_EYE, "§bToggle Vanish", "§7/vanish", "command:vanish"));
        inv.setItem(31, cmdIcon(plugin, Material.FEATHER, "§bToggle Flight", "§7/fly", "command:fly"));
        inv.setItem(32, cmdIcon(plugin, Material.GOLDEN_APPLE, "§bHeal Self", "§7/heal", "command:heal"));
        inv.setItem(33, cmdIcon(plugin, Material.GRASS_BLOCK, "§bCreative Mode", "§7/gamemode creative", "command:gamemode creative"));
        inv.setItem(34, cmdIcon(plugin, Material.IRON_SWORD, "§bSurvival Mode", "§7/gamemode survival", "command:gamemode survival"));

        inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }

    private ItemStack cmdIcon(AllyPhonePlugin plugin, Material material, String name, String lore, String command) {
        ItemStack icon = GuiUtil.icon(material, name, lore);
        return GuiUtil.tagged(plugin, icon, GuiUtil.ACTION_KEY, "command:" + command);
    }
}
