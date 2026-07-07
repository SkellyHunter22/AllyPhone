package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import com.allyphone.integration.TownyPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlotsApp implements PhoneApp {

    @Override
    public String getId() {
        return "plots";
    }

    @Override
    public String getDisplayName() {
        return "§a🗺 Plots";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("manage_your_plots", getDisplayName(), "§7Manage your plots");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        boolean townyPresent = Bukkit.getPluginManager().isPluginEnabled("Towny");
        boolean plotSquaredPresent = Bukkit.getPluginManager().isPluginEnabled("PlotSquared")
                || Bukkit.getPluginManager().isPluginEnabled("Plots");

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§a§lPlots");
        holder.setInventory(inv);

        if (townyPresent) {
            String townName = TownyPlaceholders.resolve(player, "%towny_plot_town%", "wilderness");
            String plotOwner = TownyPlaceholders.resolve(player, "%towny_plot_owner%", "unclaimed");
            String plotName = TownyPlaceholders.resolve(player, "%towny_plot_name%", "-");

            inv.setItem(4, GuiUtil.icon("standing_in", "§aStanding in: §f" + townName,
                    "§7Plot owner: §f" + plotOwner,
                    "§7Plot name: §f" + plotName));

            inv.setItem(10, GuiUtil.tagged(plugin,
                    GuiUtil.icon("plot_info", "§ePlot Info", "§7/plot info"),
                    GuiUtil.ACTION_KEY, "command:plot info"));
            inv.setItem(12, GuiUtil.tagged(plugin,
                    GuiUtil.icon("claim_this_plot", "§eClaim This Plot", "§7/plot claim"),
                    GuiUtil.ACTION_KEY, "command:plot claim"));
            inv.setItem(14, GuiUtil.tagged(plugin,
                    GuiUtil.icon("put_plot_for_sale", "§ePut Plot For Sale", "§7/plot forsale"),
                    GuiUtil.ACTION_KEY, "command:plot forsale"));
            inv.setItem(16, GuiUtil.tagged(plugin,
                    GuiUtil.icon("unclaim_this_plot", "§eUnclaim This Plot", "§7/plot unclaim"),
                    GuiUtil.ACTION_KEY, "command:plot unclaim"));
        } else if (plotSquaredPresent) {
            inv.setItem(13, GuiUtil.icon("plotsquared_detected", "§aPlotSquared detected",
                    "§7Use §f/plot §7in-game", "§7to manage your plots."));
        } else {
            inv.setItem(13, GuiUtil.icon("no_plot_plugin_installed", "§cNo plot plugin installed"));
        }

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }
}
