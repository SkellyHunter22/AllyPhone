package com.allyphone.commands;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PhoneNewsCommand implements CommandExecutor {

    private final AllyPhonePlugin plugin;

    public PhoneNewsCommand(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /phonenews <title> | <body>");
            return true;
        }

        String joined = String.join(" ", args);
        String[] parts = joined.split("\\|", 2);
        String title = parts[0].trim();
        String body = parts.length > 1 ? parts[1].trim() : "";

        if (title.isEmpty()) {
            sender.sendMessage("§cUsage: /phonenews <title> | <body>");
            return true;
        }

        if (plugin.getNewsService().post(sender.getName(), title, body)) {
            sender.sendMessage("§aNews posted.");
        } else {
            sender.sendMessage("§cFailed to post news.");
        }
        return true;
    }
}
