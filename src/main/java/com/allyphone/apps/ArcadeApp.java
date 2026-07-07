package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/** Mini-games for fun: coin flip, dice roll, and rock-paper-scissors vs. the phone. No stakes, no dependencies. */
public class ArcadeApp implements PhoneApp {

    @Override
    public String getId() {
        return "arcade";
    }

    @Override
    public String getDisplayName() {
        return "§6🕹 Arcade";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("coin_flip_dice_rps", getDisplayName(), "§7Coin flip, dice & RPS");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public void open(Player player) {
        open(player, null);
    }

    /** Renders the arcade; {@code result} is the outcome line of the last game, or null. */
    public static void open(Player player, String result) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 45, "§6§lArcade");
        holder.setInventory(inv);

        inv.setItem(11, GuiUtil.tagged(plugin,
                GuiUtil.icon("coin_flip", "§eCoin Flip", "§7Heads or tails?", "§eClick to flip"),
                GuiUtil.ACTION_KEY, "arcade:flip"));
        inv.setItem(13, GuiUtil.tagged(plugin,
                GuiUtil.icon("dice_roll", "§fDice Roll", "§7Roll two six-sided dice", "§eClick to roll"),
                GuiUtil.ACTION_KEY, "arcade:dice"));

        inv.setItem(15, GuiUtil.icon("rock_paper_scissors", "§bRock · Paper · Scissors", "§7Pick your move below", "§7and beat the phone!"));
        inv.setItem(24, GuiUtil.tagged(plugin,
                GuiUtil.icon("rock", "§7Rock", "§eClick to throw rock"),
                GuiUtil.ACTION_KEY, "arcade:rps:rock"));
        inv.setItem(25, GuiUtil.tagged(plugin,
                GuiUtil.icon("paper", "§fPaper", "§eClick to throw paper"),
                GuiUtil.ACTION_KEY, "arcade:rps:paper"));
        inv.setItem(26, GuiUtil.tagged(plugin,
                GuiUtil.icon("scissors", "§cScissors", "§eClick to throw scissors"),
                GuiUtil.ACTION_KEY, "arcade:rps:scissors"));

        if (result != null) {
            inv.setItem(31, GuiUtil.icon("result", "§d§lResult", result));
        }

        inv.setItem(40, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }

    /** Handles "flip", "dice", or "rps:<move>" (the part after the "arcade:" action prefix). */
    public static void play(Player player, String game) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (game.equals("flip")) {
            open(player, random.nextBoolean() ? "§eThe coin landed on §6§lHEADS§e!" : "§eThe coin landed on §7§lTAILS§e!");
        } else if (game.equals("dice")) {
            int a = random.nextInt(1, 7);
            int b = random.nextInt(1, 7);
            open(player, "§fYou rolled §b" + a + " §fand §b" + b + " §f— total §b§l" + (a + b) + "§f!");
        } else if (game.startsWith("rps:")) {
            open(player, playRps(game.substring("rps:".length()), random));
        }
    }

    private static String playRps(String playerMove, ThreadLocalRandom random) {
        String[] moves = {"rock", "paper", "scissors"};
        String phoneMove = moves[random.nextInt(3)];
        String line = "§7You: §f" + playerMove + " §7· Phone: §f" + phoneMove + " §7— ";
        if (playerMove.equals(phoneMove)) return line + "§e§lTIE!";
        boolean playerWins = (playerMove.equals("rock") && phoneMove.equals("scissors"))
                || (playerMove.equals("paper") && phoneMove.equals("rock"))
                || (playerMove.equals("scissors") && phoneMove.equals("paper"));
        return line + (playerWins ? "§a§lYOU WIN!" : "§c§lPHONE WINS!");
    }
}
