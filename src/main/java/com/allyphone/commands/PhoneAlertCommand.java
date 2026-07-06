package com.allyphone.commands;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Console-only bridge other plugins call into to push a notification onto a
 * player's phone: {@code phonealert <player> <source> <message...>}.
 * Not player-facing; other plugins dispatch it via Bukkit.dispatchCommand.
 */
public class PhoneAlertCommand implements CommandExecutor {

    private final AllyPhonePlugin plugin;

    public PhoneAlertCommand(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: phonealert <player> <source> <message...>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("phonealert: unknown player " + args[0]);
            return true;
        }

        String source = args[1];
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        plugin.getAlertService().push(target.getUniqueId(), source, message);
        return true;
    }
}
