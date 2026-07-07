package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.BankingService;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Real-money casino: a 3-reel slot machine and a double-or-nothing coin flip, wagering
 * through the server's banking plugin. Stakes and whether the app is offered at all are
 * controlled by 'casino.*' in config.yml so server owners can tune or disable gambling.
 */
public class CasinoApp implements PhoneApp {

    /** One symbol on the reels: its icon, weight (higher = more common), and payout multiplier for 3-of-a-kind / 2-of-a-kind. */
    private record Symbol(Material material, String label, int weight, double tripleMultiplier, double pairMultiplier) {
    }

    private static final List<Symbol> SYMBOLS = List.of(
            new Symbol(Material.MELON_SLICE, "§aCherries", 35, 2, 1),
            new Symbol(Material.APPLE, "§eLemons", 28, 3, 1),
            new Symbol(Material.GOLD_INGOT, "§6Bells", 18, 5, 1.5),
            new Symbol(Material.EMERALD, "§2Clovers", 12, 10, 2),
            new Symbol(Material.DIAMOND, "§bDiamonds", 6, 25, 3),
            new Symbol(Material.NETHER_STAR, "§d§lJACKPOT", 1, 100, 5));

    private static final int TOTAL_WEIGHT = SYMBOLS.stream().mapToInt(Symbol::weight).sum();

    @Override
    public String getId() {
        return "casino";
    }

    @Override
    public String getDisplayName() {
        return "§4🎰 Casino";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("slots_double_or_nothing", getDisplayName(), "§7Slots & double-or-nothing", "§c§oPlay responsibly!");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        open(player, (String[]) null);
    }

    public static void open(Player player, String... result) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        BankingService bank = plugin.getBankingService();

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§4§lCasino");
        holder.setInventory(inv);

        if (bank == null) {
            inv.setItem(22, GuiUtil.icon("no_banking_plugin_installed", "§cNo banking plugin installed",
                    "§7Install Vault or AlsBanker", "§7to enable the Casino app."));
            inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
            GuiUtil.addThemedBezel(inv, player);
            player.openInventory(inv);
            return;
        }

        double balance = bank.getBalance(player);
        inv.setItem(4, GuiUtil.icon("your_balance", "§6§lYour Balance", "§f" + bank.format(balance)));

        List<Double> bets = plugin.getConfig().getDoubleList("casino.bets").stream()
                .filter(bet -> bet > 0)
                .distinct()
                .toList();
        if (bets.isEmpty()) bets = List.of(10.0, 50.0, 100.0, 500.0);

        inv.setItem(10, GuiUtil.icon("slot_machine", "§e§lSlot Machine",
                "§7Match 3 to win big, 2 for a small payout.",
                "§7Bet options below."));
        int slotSlot = 19;
        for (double bet : bets) {
            if (slotSlot > 25) break;
            inv.setItem(slotSlot++, GuiUtil.tagged(plugin,
                    GuiUtil.icon("bet", "§eBet " + bank.format(bet), "§7Click to spin"),
                    GuiUtil.ACTION_KEY, "casino:slots:" + bet));
        }

        inv.setItem(13, GuiUtil.icon("double_or_nothing", "§b§lDouble or Nothing",
                "§7Flip a coin: win and double your bet,", "§7lose and it's gone.",
                "§7Bet options below."));
        int flipSlot = 28;
        for (double bet : bets) {
            if (flipSlot > 34) break;
            inv.setItem(flipSlot++, GuiUtil.tagged(plugin,
                    GuiUtil.icon("bet_2", "§bBet " + bank.format(bet), "§7Click to flip"),
                    GuiUtil.ACTION_KEY, "casino:flip:" + bet));
        }

        if (result != null && result.length > 0) {
            inv.setItem(40, GuiUtil.icon("result", "§d§lResult", result));
        }

        inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }

    /** Handles "slots:<amount>" or "flip:<amount>" (the part after the "casino:" action prefix). */
    public static void play(Player player, String action) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        BankingService bank = plugin.getBankingService();
        if (bank == null) {
            open(player, (String[]) null);
            return;
        }

        String[] parts = action.split(":", 2);
        if (parts.length != 2) return;
        double bet;
        try {
            bet = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }
        if (bet <= 0) return;

        if (!bank.withdraw(player, bet)) {
            open(player, "§cYou don't have " + bank.format(bet) + " to bet!");
            return;
        }

        if (parts[0].equals("slots")) {
            spinSlots(player, bank, bet);
        } else if (parts[0].equals("flip")) {
            flipCoin(player, bank, bet);
        } else {
            bank.deposit(player, bet);
            open(player, (String[]) null);
        }
    }

    /** Ticks to hold on a "spinning..." screen before revealing the outcome, for suspense. */
    private static final long SUSPENSE_TICKS = 25L;

    private static void spinSlots(Player player, BankingService bank, double bet) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Symbol a = roll(random);
        Symbol b = roll(random);
        Symbol c = roll(random);

        open(player, "§7?? §7| §7?? §7| §7?? §8(spinning...)");
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);

        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            String reels = a.label() + " §7| " + b.label() + " §7| " + c.label();
            double payout = 0;
            if (a == b && b == c) {
                payout = bet * a.tripleMultiplier();
            } else if (a == b || b == c || a == c) {
                Symbol pair = a == b ? a : (b == c ? b : a);
                payout = bet * pair.pairMultiplier();
            }

            String outcome;
            if (payout > 0) {
                bank.deposit(player, payout);
                double net = payout - bet;
                if (net > 0) {
                    outcome = "§a§lWIN! §f+" + bank.format(net);
                    player.playSound(player.getLocation(),
                            a == SYMBOLS.get(SYMBOLS.size() - 1) ? Sound.ENTITY_PLAYER_LEVELUP : Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                            1f, 1f);
                } else {
                    outcome = "§e§lBROKE EVEN §7(refunded " + bank.format(payout) + ")";
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                }
            } else {
                outcome = "§c§lNo match. §7-" + bank.format(bet);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }

            open(player, reels, outcome);
        }, SUSPENSE_TICKS);
    }

    private static void flipCoin(Player player, BankingService bank, double bet) {
        // Slight house edge: player wins 48% of flips.
        boolean win = ThreadLocalRandom.current().nextInt(100) < 48;

        open(player, "§7Flipping...");
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1f);

        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            String outcome;
            if (win) {
                bank.deposit(player, bet * 2);
                outcome = "§a§lHEADS UP! §fYou won §f" + bank.format(bet) + "!";
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            } else {
                outcome = "§c§lTAILS. §7You lost " + bank.format(bet) + ".";
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
            open(player, outcome);
        }, SUSPENSE_TICKS);
    }

    private static Symbol roll(ThreadLocalRandom random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        int cumulative = 0;
        for (Symbol symbol : SYMBOLS) {
            cumulative += symbol.weight();
            if (roll < cumulative) return symbol;
        }
        return SYMBOLS.get(SYMBOLS.size() - 1);
    }
}
