package com.allyphone.commands;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.service.AtmStore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class AtmCommand implements CommandExecutor {

    private final AllyPhonePlugin plugin;

    public AtmCommand(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /atm <add|remove|list> [name]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "list" -> list(sender);
            default -> sender.sendMessage("§cUsage: /atm <add|remove|list> [name]");
        }
        return true;
    }

    private void add(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can add an ATM (uses your current location).");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /atm add <name>");
            return;
        }
        try {
            plugin.getAtmStore().add(args[1], player.getLocation());
            sender.sendMessage("§aATM '" + args[1] + "' registered at your location.");
        } catch (SQLException e) {
            sender.sendMessage("§cFailed to add ATM: " + e.getMessage());
        }
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /atm remove <name>");
            return;
        }
        try {
            boolean removed = plugin.getAtmStore().removeByName(args[1]);
            sender.sendMessage(removed ? "§aATM removed." : "§cNo ATM named '" + args[1] + "' found.");
        } catch (SQLException e) {
            sender.sendMessage("§cFailed to remove ATM: " + e.getMessage());
        }
    }

    private void list(CommandSender sender) {
        try {
            List<AtmStore.Atm> atms = plugin.getAtmStore().getAll();
            if (atms.isEmpty()) {
                sender.sendMessage("§7No ATMs registered.");
                return;
            }
            for (AtmStore.Atm atm : atms) {
                sender.sendMessage("§e" + atm.name() + " §7- " + atm.world() + " ("
                        + atm.x() + ", " + atm.y() + ", " + atm.z() + ")");
            }
        } catch (SQLException e) {
            sender.sendMessage("§cFailed to list ATMs: " + e.getMessage());
        }
    }
}
