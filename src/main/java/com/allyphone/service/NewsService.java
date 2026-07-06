package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.sql.NewsSQLService;

import java.sql.SQLException;
import java.util.List;

/** Wraps {@link NewsSQLService} with broadcast + Discord notification behavior. */
public class NewsService {

    private final AllyPhonePlugin plugin;
    private final NewsSQLService newsSqlService;

    public NewsService(AllyPhonePlugin plugin, NewsSQLService newsSqlService) {
        this.plugin = plugin;
        this.newsSqlService = newsSqlService;
    }

    public boolean post(String author, String title, String body) {
        try {
            newsSqlService.insert(author, title, body);
        } catch (SQLException e) {
            plugin.getLogger().warning("NewsService: failed to store news post: " + e.getMessage());
            return false;
        }
        plugin.getServer().broadcastMessage("§6§l[City News] §e" + title + " §7- " + author);
        plugin.getDiscordWebhook().postNews(title, body, author);
        return true;
    }

    public List<NewsSQLService.NewsPost> getRecent(int limit) {
        try {
            return newsSqlService.getRecent(limit);
        } catch (SQLException e) {
            plugin.getLogger().warning("NewsService: failed to load news: " + e.getMessage());
            return List.of();
        }
    }
}
