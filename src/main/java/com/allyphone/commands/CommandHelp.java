package com.allyphone.commands;

import org.bukkit.command.CommandSender;

/** Prints a simple, permission-aware command list. Reachable via "/phone help" (or any command's "help" arg). */
final class CommandHelp {

    private CommandHelp() {
    }

    static void send(CommandSender sender) {
        sender.sendMessage("§7§m                                                §r §f§lAllyPhone Commands §7§m                                                ");
        sender.sendMessage("§e/phone §7- Open your AllyPhone");
        sender.sendMessage("§e/phone get §7- Get an AllyPhone if you don't have one");
        sender.sendMessage("§e/sms <player> <message> §7- Send an SMS");

        if (sender.hasPermission("allyphone.atm")) {
            sender.sendMessage("§e/atm <add|remove|list> [name] §7- Manage ATM locations");
        }
        if (sender.hasPermission("allyphone.celltower")) {
            sender.sendMessage("§e/celltower <add|remove|list> [name] [radius] §7- Manage cell towers");
        }
        if (sender.hasPermission("allyphone.news")) {
            sender.sendMessage("§e/phonenews <title> | <body> §7- Post city news");
        }
        if (sender.hasPermission("allyphone.admin")) {
            sender.sendMessage("§e/phone reload [full] §7- Reload config, or hand off to PlugManX for a full jar reload");
        }
    }
}
