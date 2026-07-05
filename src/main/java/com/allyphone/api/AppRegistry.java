package com.allyphone.api;

import java.util.*;

public class AppRegistry {

    private final Map<String, PhoneApp> apps = new LinkedHashMap<>();

    public void registerApp(PhoneApp app) {
        apps.put(app.getId().toLowerCase(), app);
    }

    public PhoneApp getApp(String id) {
        return apps.get(id.toLowerCase());
    }

    public Collection<PhoneApp> getAllApps() {
        return apps.values();
    }
}
