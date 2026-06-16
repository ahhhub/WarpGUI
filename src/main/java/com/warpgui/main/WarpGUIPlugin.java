package com.warpgui.main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class WarpGUIPlugin extends JavaPlugin implements Listener {
    private static WarpGUIPlugin instance;
    DatabaseManager databaseManager;
    WarpManager warpManager;
    LangManager langManager;

    @Override
    public void onEnable() {
        instance = this;
        releaseDefaultResources();
        saveDefaultConfig();
        reloadConfig();

        langManager = new LangManager(this);
        langManager.loadLang();

        databaseManager = new DatabaseManager(this);
        databaseManager.initDatabase();

        warpManager = new WarpManager(this, databaseManager);
        warpManager.loadWarps();

        WarpCommand warpCommand = new WarpCommand(this, warpManager);
        getCommand("warpgui").setExecutor(warpCommand);
        getCommand("warpgui").setTabCompleter(warpCommand);
        getServer().getPluginManager().registerEvents(new MenuListener(this, warpManager), this);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("WarpGUI 插件已成功加载！");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.closeConnection();
        MenuListener.clearAllPlayerStates();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        MenuListener.clearPlayerState(event.getPlayer().getUniqueId());
    }

    public void reloadPlugin() {
        reloadConfig();
        langManager.loadLang();
        databaseManager.closeConnection();
        databaseManager.initDatabase();
        warpManager.loadWarps();
        MenuListener.clearAllPlayerStates();
    }

    private void releaseDefaultResources() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) langFolder.mkdirs();
        String langCode = getConfig().getString("lang", "zh-cn");
        File langFile = new File(langFolder, langCode + ".yml");
        if (!langFile.exists()) {
            String resourcePath = "lang/" + langCode + ".yml";
            if (getResource(resourcePath) != null) {
                saveResource(resourcePath, false);
            }
        }
    }

    public LangManager getLangManager() { return langManager; }
    public static WarpGUIPlugin getInstance() { return instance; }
}