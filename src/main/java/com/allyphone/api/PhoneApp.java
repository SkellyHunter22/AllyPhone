package com.allyphone.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PhoneApp {

    String getId();                 // Unique ID, e.g. "wallet"
    String getDisplayName();        // GUI name, e.g. "💰 Wallet"
    ItemStack getIcon(Player viewer); // Icon shown on the phone home screen

    boolean requiresService();      // true = needs signal + paid bill

    void open(Player player);       // Open the app GUI
}
