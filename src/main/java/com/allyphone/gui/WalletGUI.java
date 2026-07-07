package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.BankingService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.List;

public class WalletGUI {

    private WalletGUI() {
    }

    public static void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        BankingService bank = plugin.getBankingService();

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 45, "§2§lWallet");
        holder.setInventory(inv);

        if (bank == null) {
            inv.setItem(22, GuiUtil.icon("no_banking_plugin_installed", "§cNo banking plugin installed",
                    "§7Install Vault or AlsBanker", "§7to enable the Wallet app."));
        } else {
            double balance = bank.getBalance(player);
            inv.setItem(10, GuiUtil.icon("balance", "§6§lBalance",
                    "§f" + bank.format(balance), "§7via " + bank.getName()));

            BankingService.LoanSummary loan = bank.getLoanSummary(player);
            if (loan.hasActiveLoan()) {
                inv.setItem(12, GuiUtil.icon("active_loan", "§c§lActive Loan",
                        "§7Outstanding: §f" + bank.format(loan.outstanding()),
                        "§7Next due: §f" + loan.nextDueDate(),
                        "§7Amount due: §f" + bank.format(loan.nextAmountDue())));
            } else {
                inv.setItem(12, GuiUtil.icon("no_active_loan", "§a§lNo Active Loan"));
            }

            BankingService.SavingsSummary savings = bank.getSavingsSummary(player);
            if (savings.available()) {
                inv.setItem(14, GuiUtil.icon("savings", "§b§lSavings",
                        "§f" + bank.format(savings.balance()),
                        "§7Daily interest: " + String.format("%.1f", savings.dailyInterestRate() * 100) + "%"));
            } else {
                inv.setItem(14, GuiUtil.icon("savings", "§7Savings",
                        "§8No savings account yet.", "§7Open one via AlsBanker."));
            }

            List<BankingService.StockHolding> stocks = bank.getStockHoldings(player);
            if (stocks.isEmpty()) {
                inv.setItem(16, GuiUtil.icon("stocks", "§7Stocks",
                        "§8You don't own any stocks yet.", "§7Buy some via AlsBanker."));
            } else {
                List<String> lore = new java.util.ArrayList<>();
                for (BankingService.StockHolding holding : stocks) {
                    lore.add("§f" + holding.symbol() + " §7x" + holding.shares()
                            + " @ " + bank.format(holding.currentPrice()));
                }
                inv.setItem(16, GuiUtil.icon("stocks", "§b§lStocks", lore.toArray(new String[0])));
            }

            List<BankingService.TransactionEntry> history = bank.getTransactionHistory(player, 5);
            int slot = 19;
            if (history.isEmpty()) {
                inv.setItem(slot, GuiUtil.icon("no_recent_transactions", "§7No recent transactions"));
            } else {
                for (BankingService.TransactionEntry tx : history) {
                    if (slot > 23) break;
                    inv.setItem(slot++, GuiUtil.icon("book", "§e" + tx.type() + " §7" + tx.timestamp(),
                            "§7Amount: §f" + bank.format(tx.amount()),
                            "§7Balance after: §f" + bank.format(tx.balanceAfter()),
                            "§7" + tx.description()));
                }
            }
        }

        boolean serviceActive;
        try {
            serviceActive = plugin.getServicePlanService().isServiceActive(player.getUniqueId());
        } catch (SQLException e) {
            serviceActive = true;
        }
        inv.setItem(29, GuiUtil.tagged(plugin,
                GuiUtil.icon(serviceActive ? "billing_active" : "pay_phone_bill",
                        serviceActive ? "§aService Active" : "§c§lPay Phone Bill",
                        serviceActive ? "§7Nothing due right now." : "§eClick to pay your bill now"),
                GuiUtil.ACTION_KEY, "paybill"));

        inv.setItem(31, GuiUtil.tagged(plugin,
                GuiUtil.icon("find_nearest_atm", "§a§lFind Nearest ATM", "§7Withdraw cash in person via AlsBanker"),
                GuiUtil.ACTION_KEY, "findatm"));

        inv.setItem(33, GuiUtil.tagged(plugin,
                GuiUtil.icon("credit_loans", "§6§lCredit & Loans", "§7Credit score, loans, and credit card"),
                GuiUtil.ACTION_KEY, "credit"));

        inv.setItem(40, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }
}
