package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.MessagesGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MessagesApp implements PhoneApp {

    @Override
    public String getId() {
        return "messages";
    }

    @Override
    public String getDisplayName() {
        return "§b✉ Messages";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        int unread = AllyPhonePlugin.get().getMessageService().getUnreadCount(viewer.getUniqueId());
        return GuiUtil.icon(Material.WRITABLE_BOOK, getDisplayName(),
                unread > 0 ? "§e" + unread + " unread" : "§7No new messages");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public boolean isEssential() {
        return true;
    }

    @Override
    public void open(Player player) {
        MessagesGUI.open(player);
    }
}
