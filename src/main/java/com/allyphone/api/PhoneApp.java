package com.allyphone.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PhoneApp {

    String getId();                 // Unique ID, e.g. "wallet"
    String getDisplayName();        // GUI name, e.g. "💰 Wallet"
    ItemStack getIcon(Player viewer); // Icon shown on the phone home screen

    boolean requiresService();      // true = needs signal + paid bill

    void open(Player player);       // Open the app GUI

    /** True if this app is a core system app and cannot be uninstalled by players. */
    default boolean isEssential() {
        return false;
    }

    /** Permission required to see/use this app at all (e.g. an admin-only app), or null if anyone can. */
    default String requiredPermission() {
        return null;
    }
}
