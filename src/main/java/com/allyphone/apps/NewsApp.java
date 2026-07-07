package com.allyphone.apps;

import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.NewsGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NewsApp implements PhoneApp {

    @Override
    public String getId() {
        return "news";
    }

    @Override
    public String getDisplayName() {
        return "§6📰 City News";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("latest_announcements", getDisplayName(), "§7Latest announcements");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        NewsGUI.open(player);
    }
}
