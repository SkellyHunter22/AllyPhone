package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.sql.MessageSQLService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagesGUI {

    private MessagesGUI() {
    }

    public static void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§b§lMessages");
        holder.setInventory(inv);

        List<MessageSQLService.StoredMessage> inbox;
        try {
            inbox = plugin.getMessageSqlService().getInbox(player.getUniqueId(), 45);
            plugin.getMessageSqlService().markAllRead(player.getUniqueId());
        } catch (SQLException e) {
            plugin.getLogger().warning("MessagesGUI: failed to load inbox: " + e.getMessage());
            inbox = List.of();
        }

        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm");
        int slot = 0;
        if (inbox.isEmpty()) {
            inv.setItem(4, GuiUtil.icon(Material.PAPER, "§7No messages yet",
                    "§7Use §f/sms <player> <message>", "§7to send one!"));
        } else {
            for (MessageSQLService.StoredMessage msg : inbox) {
                if (slot >= 45) break;
                inv.setItem(slot++, GuiUtil.icon(Material.WRITABLE_BOOK,
                        "§e" + msg.senderName() + " §7" + format.format(new Date(msg.sentAt())),
                        wrap(msg.body())));
            }
        }

        inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }

    private static String wrap(String body) {
        return "§f" + (body.length() > 40 ? body.substring(0, 37) + "..." : body);
    }
}
