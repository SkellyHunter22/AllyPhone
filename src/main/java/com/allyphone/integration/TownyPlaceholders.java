package com.allyphone.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Reads Towny data via its PlaceholderAPI expansion instead of Towny's own Java API,
 * since Towny isn't bundled as a compile dependency. Safe no-op if PlaceholderAPI
 * (or its Towny expansion) isn't installed.
 */
public final class TownyPlaceholders {

    private TownyPlaceholders() {
    }

    public static String resolve(Player player, String placeholder, String fallback) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return fallback;
        }
        try {
            String result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, placeholder);
            if (result == null || result.isBlank() || result.equals(placeholder)) {
                return fallback;
            }
            return result;
        } catch (Throwable t) {
            return fallback;
        }
    }
}
