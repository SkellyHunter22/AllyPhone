package com.allyphone.apps;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.api.PhoneApp;
import com.allyphone.gui.GuiUtil;
import com.allyphone.gui.PhoneGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** GPS/navigation readout: coordinates, facing, biome, world time, and distance to bed & world spawn. */
public class GpsApp implements PhoneApp {

    @Override
    public String getId() {
        return "gps";
    }

    @Override
    public String getDisplayName() {
        return "§a🧭 GPS";
    }

    @Override
    public ItemStack getIcon(Player viewer) {
        return GuiUtil.icon("where_am_i", getDisplayName(), "§7Where am I?");
    }

    @Override
    public boolean requiresService() {
        return true;
    }

    @Override
    public void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();
        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, "§a§lGPS");
        holder.setInventory(inv);

        Location loc = player.getLocation();

        inv.setItem(10, GuiUtil.icon("position", "§ePosition",
                "§7World: §f" + loc.getWorld().getName(),
                "§7X: §f" + loc.getBlockX() + " §7Y: §f" + loc.getBlockY() + " §7Z: §f" + loc.getBlockZ(),
                "§7Facing: §f" + facing(loc.getYaw()),
                "§7Biome: §f" + prettify(loc.getBlock().getBiome().getKey().getKey())));

        long time = loc.getWorld().getTime();
        inv.setItem(12, GuiUtil.icon("time", "§eTime",
                "§7In-game: §f" + clockTime(time),
                "§7" + (time >= 12542 && time <= 23459 ? "Night — beds work" : "Daytime")));

        Location spawn = loc.getWorld().getSpawnLocation();
        inv.setItem(14, GuiUtil.icon("world_spawn", "§eWorld Spawn",
                "§7At §f" + spawn.getBlockX() + ", " + spawn.getBlockZ(),
                "§7Distance: §f" + (int) flatDistance(loc, spawn) + " blocks"));

        Location bed = player.getRespawnLocation();
        if (bed != null && bed.getWorld() != null && bed.getWorld().equals(loc.getWorld())) {
            inv.setItem(16, GuiUtil.icon("your_bed", "§eYour Bed",
                    "§7At §f" + bed.getBlockX() + ", " + bed.getBlockZ(),
                    "§7Distance: §f" + (int) flatDistance(loc, bed) + " blocks"));
        } else {
            inv.setItem(16, GuiUtil.icon("your_bed", "§7Your Bed",
                    bed == null ? "§8No bed set." : "§8In another world."));
        }

        inv.setItem(22, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.ACTION_KEY, "back"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }

    private static String facing(float yaw) {
        double rotation = (yaw % 360 + 360) % 360;
        String[] directions = {"South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East"};
        return directions[(int) Math.round(rotation / 45.0) % 8];
    }

    private static String clockTime(long ticks) {
        long hours = (ticks / 1000 + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    }

    private static double flatDistance(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static String prettify(String enumName) {
        String[] words = enumName.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}
