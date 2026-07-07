package com.allyphone.api;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public interface BankingService {

    String getName();

    double getBalance(Player player);

    /**
     * Formats an amount using this backend's real currency (e.g. Vault's registered economy
     * name/symbol), so displayed prices always match what the server's economy plugin actually uses.
     */
    default String format(double amount) {
        return "$" + String.format("%,.2f", amount);
    }

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

    /**
     * Savings account summary, for backends that support one (e.g. a future AlsBanker version).
     * Backends without savings support return an inactive summary.
     */
    default SavingsSummary getSavingsSummary(Player player) {
        return SavingsSummary.NONE;
    }

    /**
     * Stock holdings, for backends that support a stock market (e.g. a future AlsBanker version).
     * Backends without stock support return an empty list.
     */
    default List<StockHolding> getStockHoldings(Player player) {
        return Collections.emptyList();
    }

    /**
     * Credit score snapshot, for backends that track one (e.g. AlsBanker). Backends without
     * credit tracking return a neutral default (see {@link CreditSummary#NONE}).
     */
    default CreditSummary getCreditSummary(Player player) {
        return CreditSummary.NONE;
    }

    /**
     * Credit card summary, for backends that support one (e.g. AlsBanker). Backends without
     * credit card support return an inactive summary.
     */
    default CreditCardSummary getCreditCardSummary(Player player) {
        return CreditCardSummary.NONE;
    }

    record LoanSummary(boolean hasActiveLoan, double outstanding, String nextDueDate, double nextAmountDue) {
        public static final LoanSummary NONE = new LoanSummary(false, 0, null, 0);
    }

    record TransactionEntry(String timestamp, String type, double amount, double balanceAfter, String description) {
    }

    record SavingsSummary(boolean available, double balance, double dailyInterestRate) {
        public static final SavingsSummary NONE = new SavingsSummary(false, 0, 0);
    }

    record StockHolding(String symbol, int shares, double avgCost, double currentPrice) {
    }

    record CreditSummary(boolean available, int score, String rating, double maxLoanAmount) {
        public static final CreditSummary NONE = new CreditSummary(false, 0, "-", 0);
    }

    record CreditCardSummary(boolean available, double limit, double balance, double dailyApr) {
        public static final CreditCardSummary NONE = new CreditCardSummary(false, 0, 0, 0);

        public double availableCredit() {
            return limit - balance;
        }

        public double utilization() {
            return limit <= 0 ? 0 : balance / limit;
        }
    }
}
