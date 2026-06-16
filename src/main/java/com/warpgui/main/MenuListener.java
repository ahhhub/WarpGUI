package com.warpgui.main;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.*;

public class MenuListener implements Listener {
    private final WarpGUIPlugin plugin;
    private final WarpManager warpManager;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public MenuListener(WarpGUIPlugin plugin, WarpManager warpManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
    }

    // --- GUI 打开方法 ---

    public static void openMainMenu(Player player, WarpManager wm, String searchFilter, WarpGUIPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, getGuiCfg(plugin, "main-menu-title"));
        int slot = 0;

        for (WarpData warp : wm.getWarps()) {
            if (slot >= 45) break;
            if (!warp.isPublic() && !warp.getCreatorUuid().equals(player.getUniqueId())
                    && !player.hasPermission("warpgui.gui.admin")) continue;
            if (searchFilter != null && !searchFilter.isEmpty()
                    && !warp.getName().toLowerCase().contains(searchFilter.toLowerCase())) continue;

            Material mat = Material.matchMaterial(warp.getIconMaterial());
            if (mat == null) mat = Material.GRASS_BLOCK;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MINI_MESSAGE.deserialize("<gold>" + warp.getName() + "</gold>"));
            meta.lore(Arrays.asList(
                    MINI_MESSAGE.deserialize(getGuiCfg(plugin, "warp-lore-creator")
                            .replace("{creator}", warp.getCreatorName())),
                    MINI_MESSAGE.deserialize(getGuiCfg(plugin, "warp-lore-coords")
                            .replace("{x}", String.format("%.1f", warp.getLocation().getX()))
                            .replace("{y}", String.format("%.1f", warp.getLocation().getY()))
                            .replace("{z}", String.format("%.1f", warp.getLocation().getZ()))),
                    MINI_MESSAGE.deserialize(getGuiCfg(plugin, "warp-lore-world")
                            .replace("{world}", warp.getLocation().getWorld().getName()))
            ));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        inv.setItem(45, createGuiItem(plugin, Material.COMPASS, "search-item-name", "search-item-lore"));
        inv.setItem(49, createGuiItem(plugin, Material.NETHER_STAR, "create-item-name", "create-item-lore"));
        inv.setItem(53, createGuiItem(plugin, Material.WRITABLE_BOOK, "setup-item-name", "setup-item-lore"));

        player.openInventory(inv);
    }

    public static void openSetupMenu(Player player, WarpManager wm, WarpGUIPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 45, getGuiCfg(plugin, "setup-menu-title"));
        int slot = 0;
        for (WarpData warp : wm.getWarps()) {
            if (slot >= 45) break;
            if (!warp.getCreatorUuid().equals(player.getUniqueId())
                    && !player.hasPermission("warpgui.gui.admin")) continue;

            Material mat = Material.matchMaterial(warp.getIconMaterial());
            ItemStack item = new ItemStack(mat != null ? mat : Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MINI_MESSAGE.deserialize("<yellow>" + warp.getName() + "</yellow>"));
            meta.lore(Arrays.asList(
                    MINI_MESSAGE.deserialize("<gray>状态: " + (warp.isPublic()
                            ? getGuiCfg(plugin, "public-status")
                            : getGuiCfg(plugin, "private-status")) + "</gray>"),
                    MINI_MESSAGE.deserialize(getGuiCfg(plugin, "toggle-hint")),
                    MINI_MESSAGE.deserialize(getGuiCfg(plugin, "delete-hint").replace("{name}", warp.getName()))
            ));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public static void openSearchDialog(Player player, WarpGUIPlugin plugin) {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(
                        MINI_MESSAGE.deserialize("<gold>" + getDialogCfg(plugin, "search-title") + "</gold>"))
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(
                                        MINI_MESSAGE.deserialize("<gray>" + getDialogCfg(plugin, "search-desc") + "</gray>"),
                                        300)
                        ))
                        .inputs(List.of(
                                DialogInput.text("search_name",
                                        MINI_MESSAGE.deserialize("<yellow>" + getDialogCfg(plugin, "search-input-label") + "</yellow>"))
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.multiAction(List.of(
                        ActionButton.create(
                                MINI_MESSAGE.deserialize("<green>" + getDialogCfg(plugin, "search-button") + "</green>"),
                                MINI_MESSAGE.deserialize("<gray>" + getDialogCfg(plugin, "search-button-desc") + "</gray>"),
                                50,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            if (audience instanceof Player p) {
                                                String searchText = view.getText("search_name");
                                                if (searchText != null && !searchText.isEmpty()) {
                                                    openMainMenu(p, WarpGUIPlugin.getInstance().warpManager,
                                                            searchText, plugin);
                                                } else {
                                                    openMainMenu(p, WarpGUIPlugin.getInstance().warpManager,
                                                            null, plugin);
                                                }
                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(ClickCallback.UNLIMITED_USES)
                                                .lifetime(Duration.ofMinutes(5))
                                                .build()
                                )
                        ),
                        ActionButton.create(
                                MINI_MESSAGE.deserialize("<red>" + getDialogCfg(plugin, "cancel-button") + "</red>"),
                                MINI_MESSAGE.deserialize("<gray>" + getDialogCfg(plugin, "cancel-desc") + "</gray>"),
                                50,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            if (audience instanceof Player p) {
                                                openMainMenu(p, WarpGUIPlugin.getInstance().warpManager,
                                                        null, plugin);
                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(ClickCallback.UNLIMITED_USES)
                                                .lifetime(Duration.ofMinutes(5))
                                                .build()
                                )
                        )
                ), null, 2)));
        player.showDialog(dialog);
    }

    public static void openCreateDialog(Player player, WarpGUIPlugin plugin) {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(
                        MINI_MESSAGE.deserialize("<green>" + getDialogCfg(plugin, "create-title") + "</green>"))
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(
                                        MINI_MESSAGE.deserialize("<dark_gray>" + getDialogCfg(plugin, "create-icon-hint") + "</dark_gray>"),
                                        200)
                        ))
                        .inputs(List.of(
                                DialogInput.text("warp_name",
                                        MINI_MESSAGE.deserialize("<yellow>" + getDialogCfg(plugin, "create-name-label") + "</yellow>"))
                                        .build(),
                                DialogInput.text("warp_icon",
                                        MINI_MESSAGE.deserialize("<yellow>" + getDialogCfg(plugin, "create-icon-label") + "</yellow>"))
                                        .build(),
                                DialogInput.bool("warp_public",
                                        MINI_MESSAGE.deserialize("<yellow>" + getDialogCfg(plugin, "create-public-label") + "</yellow>"))
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.multiAction(List.of(
                        ActionButton.create(
                                MINI_MESSAGE.deserialize("<green>" + getDialogCfg(plugin, "create-confirm-button") + "</green>"),
                                MINI_MESSAGE.deserialize("<gray>" + getDialogCfg(plugin, "create-confirm-desc") + "</gray>"),
                                50,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            if (audience instanceof Player p) {
                                                String name = view.getText("warp_name");
                                                String icon = view.getText("warp_icon");
                                                Boolean isPublic = view.getBoolean("warp_public");

                                                if (name == null || name.isEmpty()) {
                                                    p.sendMessage(MINI_MESSAGE.deserialize(
                                                            getMsg(plugin, "name-empty")));
                                                    return;
                                                }

                                                WarpManager wm = WarpGUIPlugin.getInstance().warpManager;
                                                if (wm.getWarp(name) != null) {
                                                    p.sendMessage(MINI_MESSAGE.deserialize(
                                                            getMsg(plugin, "warp-name-exists")));
                                                    return;
                                                }

                                                String iconMat = "GRASS_BLOCK";
                                                LangManager lang = WarpGUIPlugin.getInstance().getLangManager();
                                                if (icon != null && !icon.isEmpty()) {
                                                    Material matched = lang.matchMaterial(icon);
                                                    if (matched != null && matched.isItem()) {
                                                        iconMat = matched.name();
                                                    } else {
                                                        p.sendMessage(MINI_MESSAGE.deserialize(
                                                                getMsg(plugin, "invalid-material")));
                                                    }
                                                }

                                                boolean pub = isPublic != null ? isPublic : true;

                                                WarpData newWarp = new WarpData(
                                                        name, p.getUniqueId(), p.getName(),
                                                        p.getLocation(), iconMat, pub);
                                                wm.createWarp(newWarp);

                                                Material createdIcon = Material.matchMaterial(iconMat);
                                                String iconDisplayName = createdIcon != null
                                                        ? lang.getDisplayName(createdIcon)
                                                        : iconMat;
                                                String successMsg = getMsg(plugin, "warp-created")
                                                        .replace("{name}", name)
                                                        .replace("{icon}", iconDisplayName)
                                                        .replace("{public}", pub
                                                                ? getGuiCfg(plugin, "public-status")
                                                                : getGuiCfg(plugin, "private-status"));
                                                p.sendMessage(MINI_MESSAGE.deserialize(successMsg));

                                                openMainMenu(p, wm, null, plugin);
                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(ClickCallback.UNLIMITED_USES)
                                                .lifetime(Duration.ofMinutes(5))
                                                .build()
                                )
                        ),
                        ActionButton.create(
                                MINI_MESSAGE.deserialize("<red>" + getDialogCfg(plugin, "cancel-button") + "</red>"),
                                MINI_MESSAGE.deserialize("<gray>" + getDialogCfg(plugin, "cancel-desc") + "</gray>"),
                                50,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            if (audience instanceof Player p) {
                                                openMainMenu(p, WarpGUIPlugin.getInstance().warpManager,
                                                        null, plugin);
                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(ClickCallback.UNLIMITED_USES)
                                                .lifetime(Duration.ofMinutes(5))
                                                .build()
                                )
                        )
                ), null, 2)));
        player.showDialog(dialog);
    }

    // --- 事件处理 ---

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equals(getGuiCfg(plugin, "main-menu-title"))) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0) return;

            if (slot < 45 && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()
                    && event.getCurrentItem().getItemMeta().displayName() != null) {
                Component displayName = event.getCurrentItem().getItemMeta().displayName();
                String serialized = MiniMessage.miniMessage().serialize(displayName);
                String name = serialized.replace("<gold>", "").replace("</gold>", "").trim();
                player.performCommand("warpgui tp " + name);
                player.closeInventory();
            } else if (slot == 45) {
                openSearchDialog(player, plugin);
            } else if (slot == 49) {
                if (!player.hasPermission("warpgui.gui.create")) {
                    player.sendMessage(MINI_MESSAGE.deserialize(getMsg(plugin, "no-create-permission")));
                    return;
                }
                openCreateDialog(player, plugin);
            } else if (slot == 53) {
                openSetupMenu(player, warpManager, plugin);
            }
            return;
        }

        if (title.equals(getGuiCfg(plugin, "setup-menu-title"))) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()
                    || clicked.getItemMeta().displayName() == null) return;

            Component displayName = clicked.getItemMeta().displayName();
            String serialized = MiniMessage.miniMessage().serialize(displayName);
            String wName = serialized.replace("<yellow>", "").replace("</yellow>", "").trim();
            WarpData warp = warpManager.getWarp(wName);
            if (warp == null) return;

            if (event.isLeftClick()) {
                warp.setPublic(!warp.isPublic());
                warpManager.updateWarp(warp);
                player.sendMessage(MINI_MESSAGE.deserialize(
                        getMsg(plugin, "public-toggled")
                                .replace("{name}", warp.getName())
                                .replace("{status}", warp.isPublic()
                                        ? getGuiCfg(plugin, "public-status")
                                        : getGuiCfg(plugin, "private-status"))));
                openSetupMenu(player, warpManager, plugin);
            }
        }
    }

    // --- 工具方法 ---

    private static ItemStack createGuiItem(WarpGUIPlugin plugin, Material material, String nameKey, String loreKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MINI_MESSAGE.deserialize(getGuiCfg(plugin, nameKey)));
        meta.lore(Arrays.asList(MINI_MESSAGE.deserialize(getGuiCfg(plugin, loreKey))));
        item.setItemMeta(meta);
        return item;
    }

    static String getGuiCfg(WarpGUIPlugin plugin, String path) {
        return plugin.getConfig().getString("gui." + path, "");
    }

    private static String getDialogCfg(WarpGUIPlugin plugin, String path) {
        return plugin.getConfig().getString("dialog." + path, "");
    }

    static String getMsg(WarpGUIPlugin plugin, String path) {
        return plugin.getConfig().getString("messages." + path, "");
    }
}