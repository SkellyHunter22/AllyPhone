package com.allyphone.api;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Uses one backend for the real balance (Vault, since that's what economy placeholders like
 * %vault_eco_balance% reflect) and another purely for loan summary + transaction history
 * (AlsBanker), since Vault itself has no concept of loans.
 */
public class CompositeBankingService implements BankingService {

    private final BankingService balanceProvider;
    private final BankingService loanProvider;

    public CompositeBankingService(BankingService balanceProvider, BankingService loanProvider) {
        this.balanceProvider = balanceProvider;
        this.loanProvider = loanProvider;
    }

    @Override
    public String getName() {
        return balanceProvider.getName();
    }

    @Override
    public double getBalance(Player player) {
        return balanceProvider.getBalance(player);
    }

    @Override
    public String format(double amount) {
        return balanceProvider.format(amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        return balanceProvider.withdraw(player, amount);
    }

    @Override
    public boolean deposit(Player player, double amount) {
        return balanceProvider.deposit(player, amount);
    }

    @Override
    public LoanSummary getLoanSummary(Player player) {
        return loanProvider.getLoanSummary(player);
    }

    @Override
    public List<TransactionEntry> getTransactionHistory(Player player, int limit) {
        return loanProvider.getTransactionHistory(player, limit);
    }

    @Override
    public SavingsSummary getSavingsSummary(Player player) {
        return loanProvider.getSavingsSummary(player);
    }

    @Override
    public List<StockHolding> getStockHoldings(Player player) {
        return loanProvider.getStockHoldings(player);
    }

    @Override
    public CreditSummary getCreditSummary(Player player) {
        return loanProvider.getCreditSummary(player);
    }

    @Override
    public CreditCardSummary getCreditCardSummary(Player player) {
        return loanProvider.getCreditCardSummary(player);
    }
}
