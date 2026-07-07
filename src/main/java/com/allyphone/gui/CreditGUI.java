package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.BankingService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** Credit score, active loan, and credit card management screen — a sub-screen of Wallet. */
public class CreditGUI {

    private CreditGUI() {
    }

    public static void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        BankingService bank = plugin.getBankingService();

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 45, "§6§lCredit & Loans");
        holder.setInventory(inv);

        if (bank == null) {
            inv.setItem(22, GuiUtil.icon("no_banking_plugin_installed", "§cNo banking plugin installed",
                    "§7Install AlsBanker to enable credit & loans."));
        } else {
            BankingService.CreditSummary credit = bank.getCreditSummary(player);
            if (credit.available()) {
                inv.setItem(10, GuiUtil.icon("credit_score", "§e§lCredit Score",
                        "§7Score: §f" + credit.score() + " §7(" + credit.rating() + ")",
                        "§7Max loan: §f$" + String.format("%,.2f", credit.maxLoanAmount())));
            } else {
                inv.setItem(10, GuiUtil.icon("credit_score", "§7Credit Score",
                        "§8Not available — install AlsBanker."));
            }

            BankingService.LoanSummary loan = bank.getLoanSummary(player);
            if (loan.hasActiveLoan()) {
                inv.setItem(12, GuiUtil.icon("active_loan", "§c§lActive Loan",
                        "§7Outstanding: §f$" + String.format("%,.2f", loan.outstanding()),
                        "§7Next due: §f" + loan.nextDueDate(),
                        "§7Amount due: §f$" + String.format("%,.2f", loan.nextAmountDue())));
                inv.setItem(21, GuiUtil.tagged(plugin,
                        GuiUtil.icon("make_a_payment", "§aMake a Payment", "§7Click to pay towards your loan"),
                        GuiUtil.ACTION_KEY, "loanpay"));
            } else {
                inv.setItem(12, GuiUtil.icon("no_active_loan", "§a§lNo Active Loan"));
                String[] requestLore = credit.available()
                        ? new String[]{"§7Click to request an amount", "§7(capped at $" + String.format("%,.2f", credit.maxLoanAmount()) + ")"}
                        : new String[]{"§7Click to request an amount"};
                inv.setItem(21, GuiUtil.tagged(plugin,
                        GuiUtil.icon("request_a_loan", "§eRequest a Loan", requestLore),
                        GuiUtil.ACTION_KEY, "loanrequest"));
            }

            BankingService.CreditCardSummary card = bank.getCreditCardSummary(player);
            if (card.available()) {
                inv.setItem(14, GuiUtil.icon("credit_card", "§d§lCredit Card",
                        "§7Limit: §f$" + String.format("%,.2f", card.limit()),
                        "§7Balance: §f$" + String.format("%,.2f", card.balance()),
                        "§7Available: §f$" + String.format("%,.2f", card.availableCredit()),
                        "§7Utilization: §f" + String.format("%.0f", card.utilization() * 100) + "%",
                        "§7Daily APR: §f" + String.format("%.2f", card.dailyApr() * 100) + "%"));
                inv.setItem(23, GuiUtil.tagged(plugin,
                        GuiUtil.icon("charge_card", "§cCharge Card", "§7Click to charge an amount"),
                        GuiUtil.ACTION_KEY, "cardcharge"));
                inv.setItem(25, GuiUtil.tagged(plugin,
                        GuiUtil.icon("pay_card", "§aPay Card", "§7Click to pay down your balance"),
                        GuiUtil.ACTION_KEY, "cardpay"));
            } else {
                inv.setItem(14, GuiUtil.icon("credit_card", "§7Credit Card",
                        "§8You haven't applied for one yet."));
                inv.setItem(23, GuiUtil.tagged(plugin,
                        GuiUtil.icon("apply_for_a_card", "§eApply for a Card", "§7Click to apply"),
                        GuiUtil.ACTION_KEY, "command:creditcard apply"));
            }
        }

        inv.setItem(40, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.APP_ID_KEY, "wallet"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }
}
