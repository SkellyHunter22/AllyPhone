package com.allyphone.commands;

import com.allyphone.AllyPhonePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PhoneCommand implements CommandExecutor {

    private final AllyPhonePlugin plugin;

    public PhoneCommand(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
}
