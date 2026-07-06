package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.sql.MessageSQLService;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

/** Wraps {@link MessageSQLService} with online-notification behavior. */
public class MessageService {

    private final AllyPhonePlugin plugin;
    private final MessageSQLService messageSqlService;

    public MessageService(AllyPhonePlugin plugin, MessageSQLService messageSqlService) {
        this.plugin = plugin;
        this.messageSqlService = messageSqlService;
    }

    public boolean send(Player sender, OfflinePlayer receiver, String body) {
        try {
            messageSqlService.insert(sender.getUniqueId(), sender.getName(), receiver.getUniqueId(),
                    receiver.getName(), body);
        } catch (SQLException e) {
            plugin.getLogger().warning("MessageService: failed to store message: " + e.getMessage());
            return false;
        }

        if (receiver.isOnline() && receiver.getPlayer() != null) {
            Player online = receiver.getPlayer();
            online.sendMessage("§b§l[AllyPhone] §fNew SMS from §e" + sender.getName());
            online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        }
        return true;
    }

    public int getUnreadCount(UUID uuid) {
        try {
            return messageSqlService.countUnread(uuid);
        } catch (SQLException e) {
            plugin.getLogger().warning("MessageService: failed to count unread messages: " + e.getMessage());
            return 0;
        }
    }
}
