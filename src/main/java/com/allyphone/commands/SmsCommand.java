package com.allyphone.commands;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.service.ServicePlan;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmsCommand implements CommandExecutor, TabCompleter {

    private final AllyPhonePlugin plugin;

    public SmsCommand(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            CommandHelp.send(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /sms <player> <message>");
            return true;
        }

        if (!plugin.getSignalService().hasService(player)) {
            player.sendMessage("§cYou have no signal here.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cThat player has never joined this server.");
            return true;
        }

        String body = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        try {
            ServicePlan plan = plugin.getServicePlanService().getPlan(player.getUniqueId());
            if (plan.getSmsCost() > 0 && !plugin.getBillingService().charge(player, plan.getSmsCost())) {
                player.sendMessage("§cInsufficient funds to send SMS ($" + plan.getSmsCost() + ").");
                return true;
            }
        } catch (SQLException e) {
            player.sendMessage("§cFailed to check your service plan.");
            return true;
        }

        if (plugin.getMessageService().send(player, target, body)) {
            player.sendMessage("§aMessage sent to " + target.getName() + ".");
        } else {
            player.sendMessage("§cFailed to send message.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            return TabCompleteUtil.filter(names, args[0]);
        }
        return Collections.emptyList();
    }
}
