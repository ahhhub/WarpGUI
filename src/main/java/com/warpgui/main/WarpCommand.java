package com.warpgui.main;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class WarpCommand implements CommandExecutor, TabCompleter {
    private final WarpGUIPlugin plugin;
    private final WarpManager warpManager;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final List<String> SUB = Arrays.asList("help", "tp", "delete", "reload");

    public WarpCommand(WarpGUIPlugin plugin, WarpManager warpManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(MM.deserialize(msg("player-only")));
                return true;
            }
            if (!p.hasPermission("warpgui.gui.use")) {
                p.sendMessage(MM.deserialize(msg("no-permission")));
                return true;
            }
            MenuListener.openMainMenu(p, warpManager, 1, plugin);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help":
                for (String k : Arrays.asList("help-header","help-main","help-help","help-tp","help-delete","help-reload","help-footer"))
                    sender.sendMessage(MM.deserialize(msg(k)));
                return true;
            case "reload":
                if (!sender.hasPermission("warpgui.gui.admin")) {
                    sender.sendMessage(MM.deserialize(msg("no-admin-permission")));
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(MM.deserialize(msg("plugin-reloaded")));
                return true;
            case "tp":
                if (args.length < 2) { sender.sendMessage(MM.deserialize(msg("usage-tp"))); return true; }
                if (!(sender instanceof Player p)) { sender.sendMessage(MM.deserialize(msg("player-only"))); return true; }
                WarpData w = warpManager.getWarp(args[1]);
                if (w == null) { p.sendMessage(MM.deserialize(msg("warp-not-found"))); return true; }
                if (w.getLocation().getWorld() == null) { p.sendMessage(MM.deserialize(msg("world-not-loaded"))); return true; }
                p.teleport(w.getLocation());
                p.sendMessage(MM.deserialize(msg("warp-teleported").replace("{name}", w.getName())));
                return true;
            case "delete":
                if (args.length < 2) { sender.sendMessage(MM.deserialize(msg("usage-delete"))); return true; }
                if (!(sender instanceof Player p2)) { sender.sendMessage(MM.deserialize(msg("player-only"))); return true; }
                WarpData w2 = warpManager.getWarp(args[1]);
                if (w2 == null) { p2.sendMessage(MM.deserialize(msg("warp-not-found"))); return true; }
                boolean own = w2.getCreatorUuid().equals(p2.getUniqueId());
                if (!((own && p2.hasPermission("warpgui.gui.delete")) || p2.hasPermission("warpgui.gui.admin"))) {
                    p2.sendMessage(MM.deserialize(msg("no-delete-permission")));
                    return true;
                }
                warpManager.removeWarp(w2.getName());
                p2.sendMessage(MM.deserialize(msg("warp-deleted").replace("{name}", w2.getName())));
                return true;
            default:
                for (String k : Arrays.asList("help-header","help-main","help-help","help-tp","help-delete","help-reload","help-footer"))
                    sender.sendMessage(MM.deserialize(msg(k)));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            for (String s : SUB) if (s.startsWith(args[0].toLowerCase())) list.add(s);
            return list;
        }
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            if (args[0].equalsIgnoreCase("tp")) {
                for (WarpData w : warpManager.getWarps())
                    if (w.getName().toLowerCase().startsWith(prefix)) list.add(w.getName());
            } else if (args[0].equalsIgnoreCase("delete")) {
                for (WarpData w : warpManager.getWarps())
                    if (w.getName().toLowerCase().startsWith(prefix)) {
                        if (sender instanceof Player p) {
                            if (w.getCreatorUuid().equals(p.getUniqueId()) || p.hasPermission("warpgui.gui.admin"))
                                list.add(w.getName());
                        } else list.add(w.getName());
                    }
            }
        }
        return list;
    }

    private String msg(String path) { return plugin.getConfig().getString("messages." + path, ""); }
}