package com.allyphone.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;

/**
 * Self-hosts the resource pack bundled inside this plugin's jar (src/main/resources/resourcepack.zip)
 * over a small built-in HTTP server, so server owners don't need to upload the pack anywhere else
 * or manage a separate web host — just open the configured port and set 'resourcepack.host'.
 *
 * Minecraft clients only ever fetch resource packs via an HTTP(S) URL; this class exists purely to
 * make AllyPhone able to serve that URL itself instead of requiring external hosting.
 */
public class ResourcePackHost {

    private final JavaPlugin plugin;
    private byte[] packBytes;
    private String sha1Hex;
    private HttpServer server;

    public ResourcePackHost(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Loads the bundled pack and starts the HTTP server, if enabled in config.yml. Safe to call once at startup. */
    public void start() {
        if (!plugin.getConfig().getBoolean("resourcepack.enabled", true)) {
            return;
        }

        try (InputStream in = plugin.getResource("resourcepack.zip")) {
            if (in == null) {
                plugin.getLogger().warning("resourcepack.zip not found in plugin jar - resource pack hosting disabled.");
                return;
            }
            packBytes = in.readAllBytes();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read bundled resourcepack.zip: " + e.getMessage());
            return;
        }

        sha1Hex = sha1Hex(packBytes);

        int port = plugin.getConfig().getInt("resourcepack.port", 8181);
        HttpServer created = null;
        try {
            created = HttpServer.create(new InetSocketAddress(port), 0);
            created.createContext("/resourcepack.zip", this::handleRequest);
            // A handful of worker threads so several players downloading at once (e.g. right after
            // a restart) don't queue behind each other on the default single-threaded executor.
            created.setExecutor(Executors.newFixedThreadPool(4));
            created.start();
            server = created;
            plugin.getLogger().info("Serving bundled resource pack on port " + port + " (sha1 " + sha1Hex + ").");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to start resource pack HTTP server on port " + port + ": " + e.getMessage());
            if (created != null) created.stop(0);
            server = null;
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            server = null;
        }
    }

    /** True once the pack is loaded and being served (or would be servable by an externally configured URL). */
    public boolean isReady() {
        return packBytes != null;
    }

    /** Raw SHA-1 bytes of the pack, for {@code Player#setResourcePack}. Null if not loaded. */
    public byte[] getSha1Bytes() {
        if (sha1Hex == null) return null;
        byte[] out = new byte[20];
        for (int i = 0; i < 20; i++) {
            out[i] = (byte) Integer.parseInt(sha1Hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    /**
     * Public URL players should fetch the pack from. Uses 'resourcepack.host' from config.yml if set
     * (required for anyone outside your LAN to actually receive it); otherwise falls back to a
     * loopback/local-network guess that only works for players on the same machine/network.
     */
    public String getUrl() {
        String host = plugin.getConfig().getString("resourcepack.host", "");
        int port = plugin.getConfig().getInt("resourcepack.port", 8181);
        if (host == null || host.isBlank()) {
            host = plugin.getServer().getIp();
            if (host == null || host.isBlank()) host = "localhost";
        }
        return "http://" + host + ":" + port + "/resourcepack.zip";
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, packBytes.length);
            exchange.getResponseBody().write(packBytes);
        } finally {
            exchange.close();
        }
    }

    private static String sha1Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 unavailable", e);
        }
    }
}
