package com.allyphone.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks players who clicked a friend in the Friends app and are expected to type their SMS text next in chat. */
public class PendingSmsService {

    private final Map<UUID, String> pending = new ConcurrentHashMap<>();

    public void start(UUID sender, String targetName) {
        pending.put(sender, targetName);
    }

    /** Returns and clears the pending recipient name for this player, or null if none is pending. */
    public String consume(UUID sender) {
        return pending.remove(sender);
    }
}
