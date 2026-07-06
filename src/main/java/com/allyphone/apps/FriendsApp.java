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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class FriendsApp implements PhoneApp {

    @Override
    public String getId() {
        return "friends";
    }

    @Override
    public String getDisplayName() {
        return "§d👥 Friends";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.PLAYER_HEAD, getDisplayName(), "§7See who's online");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§d§lOnline Players");
        holder.setInventory(inv);

        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player) || slot >= 45) continue;
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(online);
            meta.setDisplayName("§e" + online.getName());
            meta.setLore(List.of("§7Click to send an SMS"));
            head.setItemMeta(meta);
            head = GuiUtil.tagged(plugin, head, GuiUtil.ACTION_KEY, "smsto:" + online.getName());
            inv.setItem(slot++, head);
        }

        if (slot == 0) {
            inv.setItem(4, GuiUtil.icon(Material.BARRIER, "§7No one else is online"));
        }

        inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
