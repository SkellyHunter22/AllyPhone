package com.allyphone.apps;

import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WeatherApp implements PhoneApp {

    @Override
    public String getId() {
        return "weather";
    }

    @Override
    public String getDisplayName() {
        return "§f☀ Weather";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("current_forecast", getDisplayName(), "§7Current forecast");
    }

    @Override
    public boolean requiresService() {
        return false;
    }

    @Override
    public void open(Player player) {
        World world = player.getWorld();
        String condition = world.hasStorm() ? (world.isThundering() ? "Thunderstorm" : "Rain") : "Clear skies";
        String period = world.getTime() < 12000 ? "Day" : "Night";
        player.sendMessage("§b§l[AllyPhone Weather] §f" + condition + " §7- " + period + " in " + world.getName());
        player.closeInventory();
    }
}
