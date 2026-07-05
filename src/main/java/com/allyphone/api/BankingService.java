package com.allyphone.api;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public interface BankingService {

    String getName();

    double getBalance(Player player);

    boolean withdraw(Player player, double amount);

    boolean deposit(Player player, double amount);

    /**
     * Loan summary for a player, if this backend supports loans. Backends without
     * loan support (e.g. Vault) return an inactive summary.
     */
    default LoanSummary getLoanSummary(Player player) {
        return LoanSummary.NONE;
    }

    /**
     * Most recent transactions for a player, newest first. Backends without
     * transaction history (e.g. Vault) return an empty list.
     */
    default List<TransactionEntry> getTransactionHistory(Player player, int limit) {
        return Collections.emptyList();
    }

    record LoanSummary(boolean hasActiveLoan, double outstanding, String nextDueDate, double nextAmountDue) {
        public static final LoanSummary NONE = new LoanSummary(false, 0, null, 0);
    }

    record TransactionEntry(String timestamp, String type, double amount, double balanceAfter, String description) {
    }
}
