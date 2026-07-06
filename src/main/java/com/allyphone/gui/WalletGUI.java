package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.BankingService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
            inv.setItem(22, GuiUtil.icon(Material.BARRIER, "§cNo banking plugin installed",
                    "§7Install Vault or AlsBanker", "§7to enable the Wallet app."));
        } else {
            double balance = bank.getBalance(player);
            inv.setItem(10, GuiUtil.icon(Material.GOLD_INGOT, "§6§lBalance",
                    "§f$" + String.format("%,.2f", balance), "§7via " + bank.getName()));

            BankingService.LoanSummary loan = bank.getLoanSummary(player);
            if (loan.hasActiveLoan()) {
                inv.setItem(12, GuiUtil.icon(Material.PAPER, "§c§lActive Loan",
                        "§7Outstanding: §f$" + String.format("%,.2f", loan.outstanding()),
                        "§7Next due: §f" + loan.nextDueDate(),
                        "§7Amount due: §f$" + String.format("%,.2f", loan.nextAmountDue())));
            } else {
                inv.setItem(12, GuiUtil.icon(Material.PAPER, "§a§lNo Active Loan"));
            }

            BankingService.SavingsSummary savings = bank.getSavingsSummary(player);
            if (savings.available()) {
                inv.setItem(14, GuiUtil.icon(Material.EMERALD, "§b§lSavings",
                        "§f$" + String.format("%,.2f", savings.balance()),
                        "§7Daily interest: " + String.format("%.1f", savings.annualInterestRate() * 100) + "%"));
            } else {
                inv.setItem(14, GuiUtil.icon(Material.EMERALD, "§7Savings",
                        "§8Not available yet - coming soon via AlsBanker."));
            }

            List<BankingService.StockHolding> stocks = bank.getStockHoldings(player);
            if (stocks.isEmpty()) {
                inv.setItem(16, GuiUtil.icon(Material.MAP, "§7Stocks",
                        "§8Not available yet - coming soon via AlsBanker."));
            } else {
                List<String> lore = new java.util.ArrayList<>();
                for (BankingService.StockHolding holding : stocks) {
                    lore.add("§f" + holding.symbol() + " §7x" + holding.shares()
                            + " @ $" + String.format("%.2f", holding.currentPrice()));
                }
                inv.setItem(16, GuiUtil.icon(Material.MAP, "§b§lStocks", lore.toArray(new String[0])));
            }

            List<BankingService.TransactionEntry> history = bank.getTransactionHistory(player, 5);
            int slot = 19;
            if (history.isEmpty()) {
                inv.setItem(slot, GuiUtil.icon(Material.BOOK, "§7No recent transactions"));
            } else {
                for (BankingService.TransactionEntry tx : history) {
                    if (slot > 23) break;
                    inv.setItem(slot++, GuiUtil.icon(Material.BOOK, "§e" + tx.type() + " §7" + tx.timestamp(),
                            "§7Amount: §f$" + String.format("%,.2f", tx.amount()),
                            "§7Balance after: §f$" + String.format("%,.2f", tx.balanceAfter()),
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
                GuiUtil.icon(serviceActive ? Material.LIME_DYE : Material.RED_DYE,
                        serviceActive ? "§aService Active" : "§c§lPay Phone Bill",
                        serviceActive ? "§7Nothing due right now." : "§eClick to pay your bill now"),
                GuiUtil.ACTION_KEY, "paybill"));

        inv.setItem(31, GuiUtil.tagged(plugin,
                GuiUtil.icon(Material.EMERALD_BLOCK, "§a§lFind Nearest ATM", "§7Withdraw cash in person via AlsBanker"),
                GuiUtil.ACTION_KEY, "findatm"));

        inv.setItem(40, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addBezel(inv);
        player.openInventory(inv);
    }
}
