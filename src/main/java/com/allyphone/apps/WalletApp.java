package com.allyphone.apps;

import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.WalletGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WalletApp implements PhoneApp {

    @Override
    public String getId() {
        return "wallet";
    }

    @Override
    public String getDisplayName() {
        return "§e💰 Wallet";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon(Material.GOLD_INGOT, getDisplayName(), "§7Balance, loans & transactions");
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
        WalletGUI.open(player);
    }
}
