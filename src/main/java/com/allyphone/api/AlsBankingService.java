package com.allyphone.api;

import com.alexander.alsbanker.AlsBanker;
import com.alexander.alsbanker.api.BankingAPI;
import com.alexander.alsbanker.api.LoanInfo;
import com.alexander.alsbanker.api.Transaction;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AlsBankingService implements BankingService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BankingAPI api;

    public AlsBankingService() {
        AlsBanker banker = AlsBanker.get();
        if (banker == null) {
            throw new IllegalStateException("AlsBanker plugin not loaded!");
        }
        this.api = banker.getBankingAPI();
    }

    @Override
    public String getName() {
        return "AlsBanker";
    }

    @Override
    public double getBalance(Player player) {
        return api.getBalance(player.getUniqueId());
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        return api.withdraw(player.getUniqueId(), amount);
    }

    @Override
    public boolean deposit(Player player, double amount) {
        return api.deposit(player.getUniqueId(), amount);
    }

    @Override
    public LoanSummary getLoanSummary(Player player) {
        LoanInfo info = api.getLoanInfo(player.getUniqueId());
        if (!info.hasActiveLoan()) {
            return LoanSummary.NONE;
        }
        String dueDate = info.getNextDueDate() != null ? info.getNextDueDate().format(DATE_FORMAT) : "-";
        return new LoanSummary(true, info.getOutstanding(), dueDate, info.getNextAmountDue());
    }

    @Override
    public List<TransactionEntry> getTransactionHistory(Player player, int limit) {
        List<Transaction> history = api.getTransactionHistory(player.getUniqueId(), limit);
        return history.stream()
                .map(t -> new TransactionEntry(
                        t.getTimestamp().format(TIMESTAMP_FORMAT),
                        t.getType(),
                        t.getAmount(),
                        t.getBalanceAfter(),
                        t.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public SavingsSummary getSavingsSummary(Player player) {
        com.alexander.alsbanker.api.SavingsInfo info = api.getSavingsInfo(player.getUniqueId());
        if (!info.available()) {
            return SavingsSummary.NONE;
        }
        return new SavingsSummary(true, info.balance(), info.dailyInterestRate());
    }

    @Override
    public List<StockHolding> getStockHoldings(Player player) {
        return api.getStockPortfolio(player.getUniqueId()).stream()
                .map(h -> new StockHolding(h.symbol(), (int) Math.round(h.shares()), h.avgCost(), h.currentPrice()))
                .collect(Collectors.toList());
    }
}
