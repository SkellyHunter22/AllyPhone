package com.allyphone.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Tracks players expected to type a free-text value next in chat (e.g. a new phone nickname). */
public class PendingInputService {

    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    public void await(UUID uuid, Consumer<String> onInput) {
        pending.put(uuid, onInput);
    }

    public boolean isPending(UUID uuid) {
        return pending.containsKey(uuid);
    }

    /** Runs and clears the pending callback for this player with the given text; no-op if none is pending. */
    public void consume(UUID uuid, String text) {
        Consumer<String> callback = pending.remove(uuid);
        if (callback != null) {
            callback.accept(text);
        }
    }

    public void cancel(UUID uuid) {
        pending.remove(uuid);
    }
}
