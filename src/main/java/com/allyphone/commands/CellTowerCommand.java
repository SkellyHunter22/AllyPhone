package com.allyphone.commands;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.service.CellTowerStore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class CellTowerCommand implements CommandExecutor {

    private final AllyPhonePlugin plugin;

    public CellTowerCommand(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /celltower <add|remove|list> [name] [radius]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "list" -> list(sender);
            default -> sender.sendMessage("§cUsage: /celltower <add|remove|list> [name] [radius]");
        }
        return true;
    }

    private void add(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can add a cell tower (uses your current location).");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /celltower add <name> [radius]");
            return;
        }
        int defaultRadius = plugin.getConfig().getInt("phone.signal-radius", 500);
        int radius = args.length >= 3 ? parseIntOr(args[2], defaultRadius) : defaultRadius;
        try {
            plugin.getCellTowerStore().add(args[1], player.getLocation(), radius, player.getUniqueId());
            plugin.getTowerMapIntegration().refresh();
            sender.sendMessage("§aCell tower '" + args[1] + "' added with radius " + radius + ".");
        } catch (SQLException e) {
            sender.sendMessage("§cFailed to add cell tower: " + e.getMessage());
        }
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /celltower remove <name>");
            return;
        }
        try {
            boolean removed = plugin.getCellTowerStore().removeByName(args[1]);
            if (removed) plugin.getTowerMapIntegration().refresh();
            sender.sendMessage(removed ? "§aCell tower removed." : "§cNo cell tower named '" + args[1] + "' found.");
        } catch (SQLException e) {
            sender.sendMessage("§cFailed to remove cell tower: " + e.getMessage());
        }
    }

    private void list(CommandSender sender) {
        try {
            List<CellTowerStore.CellTower> towers = plugin.getCellTowerStore().getAll();
            if (towers.isEmpty()) {
                sender.sendMessage("§7No cell towers registered.");
                return;
            }
            for (CellTowerStore.CellTower tower : towers) {
                sender.sendMessage("§e" + tower.name() + " §7- " + tower.world() + " ("
                        + tower.x() + ", " + tower.y() + ", " + tower.z() + ") radius " + tower.radius());
            }
        } catch (SQLException e) {
            sender.sendMessage("§cFailed to list cell towers: " + e.getMessage());
        }
    }

    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
