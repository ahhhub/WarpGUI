package com.warpgui.main;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {
    private final WarpGUIPlugin plugin;
    private final WarpManager warpManager;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final List<String> SUB_COMMANDS = Arrays.asList("help", "tp", "delete", "reload");

    public WarpCommand(WarpGUIPlugin plugin, WarpManager warpManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 无参数：打开 GUI（仅玩家）
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MINI_MESSAGE.deserialize(getMsg("player-only")));
                return true;
            }
            if (!player.hasPermission("warpgui.gui.use")) {
                player.sendMessage(MINI_MESSAGE.deserialize(getMsg("no-permission")));
                return true;
            }
            MenuListener.openMainMenu(player, warpManager, null, plugin);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                return true;

            case "reload":
                if (!sender.hasPermission("warpgui.gui.admin")) {
                    sender.sendMessage(MINI_MESSAGE.deserialize(getMsg("no-admin-permission")));
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(MINI_MESSAGE.deserialize(getMsg("plugin-reloaded")));
                return true;

            case "tp":
                if (args.length < 2) {
                    sender.sendMessage(MINI_MESSAGE.deserialize(getMsg("usage-tp")));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(MINI_MESSAGE.deserialize(getMsg("player-only")));
                    return true;
                }
                WarpData warp = warpManager.getWarp(args[1]);
                if (warp == null) {
                    player.sendMessage(MINI_MESSAGE.deserialize(getMsg("warp-not-found")));
                    return true;
                }
                if (warp.getLocation().getWorld() == null) {
                    player.sendMessage(MINI_MESSAGE.deserialize(getMsg("world-not-loaded")));
                    return true;
                }
                player.teleport(warp.getLocation());
                player.sendMessage(MINI_MESSAGE.deserialize(
                        getMsg("warp-teleported").replace("{name}", warp.getName())));
                return true;

            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(MINI_MESSAGE.deserialize(getMsg("usage-delete")));
                    return true;
                }
                if (!(sender instanceof Player player2)) {
                    sender.sendMessage(MINI_MESSAGE.deserialize(getMsg("player-only")));
                    return true;
                }
                WarpData warp2 = warpManager.getWarp(args[1]);
                if (warp2 == null) {
                    player2.sendMessage(MINI_MESSAGE.deserialize(getMsg("warp-not-found")));
                    return true;
                }
                boolean isCreator = warp2.getCreatorUuid().equals(player2.getUniqueId());
                boolean canDelete = (isCreator && player2.hasPermission("warpgui.gui.delete"))
                        || player2.hasPermission("warpgui.gui.admin");
                if (!canDelete) {
                    player2.sendMessage(MINI_MESSAGE.deserialize(getMsg("no-delete-permission")));
                    return true;
                }
                String warpName = warp2.getName();
                warpManager.removeWarp(warpName);
                player2.sendMessage(MINI_MESSAGE.deserialize(
                        getMsg("warp-deleted").replace("{name}", warpName)));
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String sub : SUB_COMMANDS) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            switch (args[0].toLowerCase()) {
                case "tp":
                    for (WarpData warp : warpManager.getWarps()) {
                        if (warp.getName().toLowerCase().startsWith(prefix)) {
                            completions.add(warp.getName());
                        }
                    }
                    return completions;
                case "delete":
                    for (WarpData warp : warpManager.getWarps()) {
                        if (warp.getName().toLowerCase().startsWith(prefix)) {
                            if (sender instanceof Player player) {
                                if (warp.getCreatorUuid().equals(player.getUniqueId())
                                        || player.hasPermission("warpgui.gui.admin")) {
                                    completions.add(warp.getName());
                                }
                            } else {
                                completions.add(warp.getName());
                            }
                        }
                    }
                    return completions;
            }
        }

        return completions;
    }

    private void sendHelp(CommandSender sender) {
        for (String key : Arrays.asList(
                "help-header",
                "help-main",
                "help-help",
                "help-tp",
                "help-delete",
                "help-reload",
                "help-footer")) {
            String line = getMsg(key);
            if (!line.isEmpty()) {
                sender.sendMessage(MINI_MESSAGE.deserialize(line));
            }
        }
    }

    private String getMsg(String path) {
        return plugin.getConfig().getString("messages." + path, "");
    }
}