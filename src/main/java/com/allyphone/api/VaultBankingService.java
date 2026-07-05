package com.allyphone.api;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class VaultBankingService implements BankingService {

    private final Economy economy;

    public VaultBankingService(Economy economy) {
        this.economy = economy;
    }

    @Override
    public String getName() {
        return "Vault (" + economy.getName() + ")";
    }

    @Override
    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean deposit(Player player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
}
