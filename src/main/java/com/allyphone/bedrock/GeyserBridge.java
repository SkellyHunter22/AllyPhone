package com.allyphone.bedrock;

import com.allyphone.AllyPhonePlugin;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineResourcePacksEvent;
import org.geysermc.geyser.api.item.custom.v2.CustomItemBedrockOptions;
import org.geysermc.geyser.api.item.custom.v2.CustomItemDefinition;
import org.geysermc.geyser.api.pack.PackCodec;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.api.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Gives Bedrock players (connected via Geyser) the same custom phone icon Java players get.
 *
 * Java's item_model component (see PhoneItem) means nothing to Bedrock clients — Geyser only shows
 * a custom icon on Bedrock if we register a matching Bedrock-format resource pack (own manifest.json,
 * item_texture.json) and a custom item definition that maps our item_model key to that pack's icon.
 * Without this bridge, Bedrock players just see a plain vanilla paper item.
 */
public final class GeyserBridge {

    private static final String BEDROCK_ICON_KEY = "allyphone_phone";
    private static final String ITEM_MODEL_NAMESPACE = "allyphone";
    private static final String ITEM_MODEL_PATH = "phone";

    private GeyserBridge() {
    }

    /**
     * No-op if Geyser-Spigot isn't installed. Safe to call unconditionally from onEnable — this is a
     * cosmetic nice-to-have for Bedrock players, so any failure here (Geyser API not ready yet, a
     * version mismatch, a locked file) must never take down the rest of AllyPhone with it.
     */
    public static void register(AllyPhonePlugin plugin) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("Geyser-Spigot")) {
            return;
        }

        try {
            Path packPath = extractBundledPack(plugin);

            EventRegistrar registrar = EventRegistrar.of(plugin);
            GeyserApi.api().eventBus().subscribe(registrar, GeyserDefineResourcePacksEvent.class, event ->
                    event.register(ResourcePack.create(PackCodec.path(packPath))));

            GeyserApi.api().eventBus().subscribe(registrar, GeyserDefineCustomItemsEvent.class, event ->
                    event.register(Identifier.of("minecraft", "paper"), CustomItemDefinition.builder(
                                    Identifier.of(ITEM_MODEL_NAMESPACE, ITEM_MODEL_PATH),
                                    Identifier.of(ITEM_MODEL_NAMESPACE, ITEM_MODEL_PATH))
                            .displayName("AllyPhone")
                            .bedrockOptions(CustomItemBedrockOptions.builder().icon(BEDROCK_ICON_KEY))
                            .build()));

            plugin.getLogger().info("Registered a Bedrock resource pack + custom item with Geyser for the phone icon.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to register phone icon with Geyser (Bedrock players will see a plain item): " + t);
        }
    }

    /** Only re-extracts if missing/changed, so a live pack mid-transfer to a connected Bedrock client is never overwritten. */
    private static Path extractBundledPack(AllyPhonePlugin plugin) throws IOException {
        Path dataFolder = plugin.getDataFolder().toPath();
        Files.createDirectories(dataFolder);
        Path target = dataFolder.resolve("bedrock_resourcepack.zip");

        try (InputStream in = plugin.getResource("bedrock_resourcepack.zip")) {
            if (in == null) {
                throw new IOException("bedrock_resourcepack.zip not found in plugin jar");
            }
            byte[] bundled = in.readAllBytes();
            if (Files.exists(target) && Arrays.equals(bundled, Files.readAllBytes(target))) {
                return target;
            }
            Files.write(target, bundled);
        }
        return target;
    }
}
