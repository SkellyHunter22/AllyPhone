package com.allyphone.service;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks plugin enable history and error/bug history on disk, independent of the server log,
 * so they survive between restarts and log rotations.
 */
public class PluginLogService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final int MAX_ENABLE_HISTORY = 3;
    private static final long ERROR_RETENTION_MILLIS = 7L * 24 * 60 * 60 * 1000;
    private static final String ENTRY_SEPARATOR = "-----";

    private static final String[] CAT_ART = {
            "     /\\_/\\  ",
            "    ( o.o ) ",
            "     > ^ <  ",
            "  AllyPhone is online!"
    };

    private final Plugin plugin;
    private final Path enableHistoryFile;
    private final Path errorLogFile;

    public PluginLogService(Plugin plugin) {
        this.plugin = plugin;
        Path logsDir = plugin.getDataFolder().toPath().resolve("logs");
        try {
            Files.createDirectories(logsDir);
        } catch (IOException e) {
            plugin.getLogger().warning("[AllyPhone] Could not create logs directory: " + e.getMessage());
        }
        this.enableHistoryFile = logsDir.resolve("enable-history.log");
        this.errorLogFile = logsDir.resolve("error-log.log");
    }

    public void printEnableArt() {
        for (String line : CAT_ART) {
            plugin.getLogger().info(line);
        }
    }

    /** Appends an enable timestamp, keeping only the most recent {@value MAX_ENABLE_HISTORY} entries. */
    public synchronized void recordEnable() {
        try {
            List<String> lines = new ArrayList<>();
            if (Files.exists(enableHistoryFile)) {
                lines.addAll(Files.readAllLines(enableHistoryFile));
            }
            lines.add(TIMESTAMP_FORMAT.format(Instant.now()) + " - AllyPhone v"
                    + plugin.getDescription().getVersion() + " enabled");
            if (lines.size() > MAX_ENABLE_HISTORY) {
                lines = lines.subList(lines.size() - MAX_ENABLE_HISTORY, lines.size());
            }
            Files.write(enableHistoryFile, lines);
        } catch (IOException e) {
            plugin.getLogger().warning("[AllyPhone] Failed to record enable history: " + e.getMessage());
        }
    }

    /** Appends a full error/bug entry with stack trace, pruning entries older than 7 days. */
    public synchronized void logError(String context, Throwable throwable) {
        try {
            long now = System.currentTimeMillis();
            List<String> kept = new ArrayList<>();
            for (String entry : readEntries()) {
                if (isWithinRetention(entry, now)) {
                    kept.add(entry);
                }
            }

            StringBuilder entryBuilder = new StringBuilder();
            entryBuilder.append(now).append('|')
                    .append(TIMESTAMP_FORMAT.format(Instant.ofEpochMilli(now)))
                    .append(" - ").append(context);
            if (throwable != null) {
                StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                entryBuilder.append(System.lineSeparator()).append(sw);
            }
            kept.add(entryBuilder.toString());

            StringBuilder out = new StringBuilder();
            for (String entry : kept) {
                out.append(entry).append(System.lineSeparator())
                        .append(ENTRY_SEPARATOR).append(System.lineSeparator());
            }
            Files.writeString(errorLogFile, out.toString());
        } catch (IOException e) {
            plugin.getLogger().warning("[AllyPhone] Failed to write error log: " + e.getMessage());
        }
    }

    private List<String> readEntries() throws IOException {
        if (!Files.exists(errorLogFile)) {
            return new ArrayList<>();
        }
        String content = Files.readString(errorLogFile);
        if (content.isBlank()) {
            return new ArrayList<>();
        }
        String separator = ENTRY_SEPARATOR + System.lineSeparator();
        List<String> entries = new ArrayList<>();
        for (String part : content.split(separator)) {
            if (!part.isBlank()) {
                entries.add(part.strip());
            }
        }
        return entries;
    }

    private boolean isWithinRetention(String entry, long now) {
        int separatorIndex = entry.indexOf('|');
        if (separatorIndex <= 0) {
            return false;
        }
        try {
            long timestamp = Long.parseLong(entry.substring(0, separatorIndex).trim());
            return now - timestamp <= ERROR_RETENTION_MILLIS;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
