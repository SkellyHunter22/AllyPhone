package com.allyphone.service;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.BankingService;
import org.bukkit.entity.Player;

public class BillingService {

    private final AllyPhonePlugin plugin;

    public BillingService(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    /** Charge a player via whichever banking backend (Vault/AlsBanker) is active, if any. */
    public boolean charge(Player player, double amount) {
        BankingService bank = plugin.getBankingService();
        if (bank == null) {
            plugin.getLogger().info("BillingService: no banking plugin, skipping charge of $" + amount + " for " + player.getName());
            return true;
        }
        if (bank.getBalance(player) < amount) {
            return false;
        }
        return bank.withdraw(player, amount);
    }

    /** Credit a player via whichever banking backend (Vault/AlsBanker) is active, if any. */
    public boolean credit(Player player, double amount) {
        BankingService bank = plugin.getBankingService();
        if (bank == null) {
            plugin.getLogger().info("BillingService: no banking plugin, skipping credit of $" + amount + " for " + player.getName());
            return true;
        }
        return bank.deposit(player, amount);
    }
}
