package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Magic 8-Ball: think of a question, shake the ball, get an answer. Purely for fun, no dependencies. */
public class EightBallApp implements PhoneApp {

    private static final List<String> ANSWERS = List.of(
            "§aIt is certain.",
            "§aWithout a doubt.",
            "§aYes, definitely.",
            "§aYou may rely on it.",
            "§aSigns point to yes.",
            "§eReply hazy, try again.",
            "§eAsk again later.",
            "§eBetter not tell you now.",
            "§eCannot predict now.",
            "§cDon't count on it.",
            "§cMy reply is no.",
            "§cMy sources say no.",
            "§cOutlook not so good.",
            "§cVery doubtful.");

    @Override
    public String getId() {
        return "8ball";
    }

    @Override
    public String getDisplayName() {
        return "§5🎱 Magic 8-Ball";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("ask_it_anything", getDisplayName(), "§7Ask it anything");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public void open(Player player) {
        open(player, null);
    }

    /** Renders the ball; {@code answer} is null before the first shake. */
    public static void open(Player player, String answer) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§5§lMagic 8-Ball");
        holder.setInventory(inv);

        inv.setItem(13, GuiUtil.tagged(plugin,
                GuiUtil.icon("the_ball", "§5§l🎱 The Ball",
                        answer == null ? "§7Think of a yes/no question," : "§7The ball says:",
                        answer == null ? "§7then click to shake." : answer,
                        "",
                        "§eClick to shake" + (answer != null ? " again" : "")),
                GuiUtil.ACTION_KEY, "8ball"));

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }

    public static void shake(Player player) {
        open(player, ANSWERS.get(ThreadLocalRandom.current().nextInt(ANSWERS.size())));
    }
}
