package com.warpgui.main;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class WarpManager {
    private final WarpGUIPlugin plugin;
    private final DatabaseManager db;
    private final Map<String, WarpData> warpCache = new ConcurrentHashMap<>();

    public WarpManager(WarpGUIPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void loadWarps() {
        warpCache.clear();
        for (WarpData warp : db.loadAllWarps()) {
            warpCache.put(warp.getName().toLowerCase(), warp);
        }
    }

    public Collection<WarpData> getWarps() {
        return warpCache.values();
    }

    public WarpData getWarp(String name) {
        return warpCache.get(name.toLowerCase());
    }

    public void createWarp(WarpData warp) {
        db.saveWarp(warp);
        warpCache.put(warp.getName().toLowerCase(), warp);
    }

    public void updateWarp(WarpData warp) {
        db.saveWarp(warp);
        warpCache.put(warp.getName().toLowerCase(), warp);
    }

    public void removeWarp(String name) {
        db.deleteWarp(name);
        warpCache.remove(name.toLowerCase());
    }
}