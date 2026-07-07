package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class ServersApp implements PhoneApp {

    @Override
    public String getId() {
        return "servers";
    }

    @Override
    public String getDisplayName() {
        return "§3🖥 Servers";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("switch_network_servers", getDisplayName(), "§7Switch network servers");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§3§lServers");
        holder.setInventory(inv);

        List<?> servers = plugin.getConfig().getList("servers");
        int slot = 0;
        if (servers == null || servers.isEmpty()) {
            inv.setItem(13, GuiUtil.icon("no_servers_configured", "§7No servers configured",
                    "§7Add entries under 'servers:' in config.yml"));
        } else {
            for (Object entry : servers) {
                if (slot >= 18 || !(entry instanceof Map<?, ?> map)) continue;
                Object name = map.get("name");
                if (name == null) continue;
                ItemStack icon = GuiUtil.icon("compass", "§b" + name, "§eClick to connect");
                icon = GuiUtil.tagged(plugin, icon, GuiUtil.ACTION_KEY, "server:" + name);
                inv.setItem(slot++, icon);
            }
        }

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }
}
