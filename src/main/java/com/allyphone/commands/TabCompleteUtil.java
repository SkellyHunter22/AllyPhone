package com.allyphone.commands;

import java.util.List;
import java.util.stream.Collectors;

/** Shared helper for filtering tab-complete suggestions by the partial argument the player has typed so far. */
final class TabCompleteUtil {

    private TabCompleteUtil() {
    }

    static List<String> filter(List<String> options, String partial) {
        String lower = partial.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
