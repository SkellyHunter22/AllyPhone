package com.allyphone.gui;

import com.allyphone.AllyPhonePlugin;
import dev.geco.gpet.GPetMain;
import dev.geco.gpet.object.GPet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

/** Lists the player's currently spawned GPet pets, keyed by GPet's own entity-id pet ID, for removal (despawn only). */
public class PetsListGUI {

    private PetsListGUI() {
    }

    public static void open(Player player) {
        AllyPhonePlugin plugin = AllyPhonePlugin.get();

        if (!Bukkit.getPluginManager().isPluginEnabled("GPet")) {
            player.closeInventory();
            player.sendMessage("§cGPet is currently unavailable.");
            return;
        }

        List<GPet> pets = GPetMain.getInstance().getPetService().getPetsByOwner(player);

        PhoneGuiHolder holder = new PhoneGuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, "§d§lMy Pets");
        holder.setInventory(inv);

        if (pets.isEmpty()) {
            inv.setItem(22, GuiUtil.icon("you_have_no_pets_out_right_now", "§7You have no pets out right now",
                    "§7Buy or spawn one from §eBuy & Manage Pets"));
        } else {
            int slot = 0;
            for (GPet pet : pets) {
                if (slot >= 45) break;
                int petId = pet.getEntity().getEntityId();
                String typeName = pet.getPetType().getName();
                String nickname = pet.getNickname();

                Material material = spawnEggFor(pet.getEntity().getType());
                inv.setItem(slot++, GuiUtil.tagged(plugin,
                        GuiUtil.icon(material,
                                nickname != null && !nickname.isBlank() ? "§e" + nickname : "§e" + typeName,
                                "§7Type: §f" + typeName,
                                "§7Pet ID: §f" + petId,
                                "§7Click to hide this pet",
                                "§7You'll still own it and can",
                                "§7respawn it any time, keeping",
                                "§7its name and data."),
                        GuiUtil.ACTION_KEY, "command:gpet remove " + petId));
            }
        }

        inv.setItem(49, GuiUtil.tagged(plugin, GuiUtil.backButton(), GuiUtil.APP_ID_KEY, "pets"));
        GuiUtil.addThemedBezel(inv, player);
        player.openInventory(inv);
    }

    private static Material spawnEggFor(org.bukkit.entity.EntityType type) {
        try {
            return Material.valueOf(type.name() + "_SPAWN_EGG");
        } catch (IllegalArgumentException e) {
            return Material.BONE;
        }
    }
}
