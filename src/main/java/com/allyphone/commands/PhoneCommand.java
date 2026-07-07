package com.allyphone.commands;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PhoneCommand implements CommandExecutor, TabCompleter {

    private final AllyPhonePlugin plugin;

    public PhoneCommand(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender, args);
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            CommandHelp.send(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("get")) {
            if (plugin.getPhoneService().hasPhone(player)) {
                player.sendMessage("§eYou already have an AllyPhone.");
            } else {
                plugin.getPhoneService().deliverPhone(player);
            }
            return true;
        }

        plugin.getPhoneService().openPhone(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = sender.hasPermission("allyphone.admin")
                    ? List.of("get", "help", "reload")
                    : List.of("get", "help");
            return TabCompleteUtil.filter(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("allyphone.admin")) {
            return TabCompleteUtil.filter(List.of("full"), args[1]);
        }
        return Collections.emptyList();
    }

    /**
     * "/phone reload" - re-reads config.yml and re-applies it live (resource pack host, banking
     * backend, app list). "/phone reload full" additionally hands off to PlugManX (if installed
     * and enabled) to unload and reload AllyPhone's classloader entirely, so a jar swapped onto
     * disk actually takes effect without a full server restart. Without PlugManX present, "full"
     * just explains that a restart is required, since Paper has no built-in way to hot-swap a
     * running plugin's code.
     */
    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("allyphone.admin")) {
            sender.sendMessage("§cYou don't have permission to do that.");
            return true;
        }

        boolean full = args.length > 1 && args[1].equalsIgnoreCase("full");

        if (!full) {
            plugin.reloadPluginState();
            sender.sendMessage("§aAllyPhone config reloaded (resource pack host, banking, apps). "
                    + "§7Use §f/phone reload full§7 to also pick up a replaced jar file.");
            return true;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("PlugManX")) {
            sender.sendMessage("§cPlugManX isn't installed/enabled, so a jar swap can't be picked up live. "
                    + "§7A full server restart is required for that - Paper has no built-in way to hot-swap a running plugin's code.");
            return true;
        }

        sender.sendMessage("§eReloading AllyPhone via PlugManX (unloading and reloading the jar from disk)...");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman reload " + plugin.getName());
        return true;
    }
}
