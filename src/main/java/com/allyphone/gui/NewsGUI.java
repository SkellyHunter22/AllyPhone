package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.sql.NewsSQLService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NewsGUI {

    private NewsGUI() {
    }

    public static void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§6§lCity News");
        holder.setInventory(inv);

        List<NewsSQLService.NewsPost> posts = plugin.getNewsService().getRecent(45);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm");

        int slot = 0;
        if (posts.isEmpty()) {
            inv.setItem(4, GuiUtil.icon("no_news_posted_yet", "§7No news posted yet"));
        } else {
            for (NewsSQLService.NewsPost post : posts) {
                if (slot >= 45) break;
                inv.setItem(slot++, GuiUtil.icon("paper", "§e" + post.title() + " §7" + format.format(new Date(post.postedAt())),
                        "§f" + (post.body().length() > 40 ? post.body().substring(0, 37) + "..." : post.body()),
                        "§8by " + post.author()));
            }
        }

        inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }
}
