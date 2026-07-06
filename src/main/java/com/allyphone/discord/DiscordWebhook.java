package com.allyphone.discord;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/** Posts city news to a Discord channel via an incoming webhook, if one is configured. */
public class DiscordWebhook {

    private final Plugin plugin;

    public DiscordWebhook(Plugin plugin) {
        this.plugin = plugin;
    }

    public void postNews(String title, String body, String author) {
        String url = plugin.getConfig().getString("discord.webhook-url", "");
        if (url == null || url.isBlank()) return;

        String content = "**" + title + "**\n" + body + "\n*- " + author + "*";
        String escaped = content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        String json = "{\"content\":\"" + escaped + "\"}";

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }
                    conn.getResponseCode();
                    conn.disconnect();
                } catch (Exception e) {
                    plugin.getLogger().warning("DiscordWebhook: failed to post news: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
