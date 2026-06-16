package com.warpgui.main;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.*;
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
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int GUI_SIZE = 54;
    private static final int ITEMS_PER_PAGE = 28;

    // 玩家状态：主界面搜索词
    private static final Map<UUID, String> mainSearch = new HashMap<>();
    // 玩家状态：主界面当前页码
    private static final Map<UUID, Integer> mainPage = new HashMap<>();
    // 设置界面搜索词
    private static final Map<UUID, String> setupSearch = new HashMap<>();
    // 设置界面页码
    private static final Map<UUID, Integer> setupPage = new HashMap<>();

    public MenuListener(WarpGUIPlugin plugin, WarpManager warpManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
    }

    // ---------- 状态清理 ----------
    public static void clearPlayerState(UUID uuid) {
        mainSearch.remove(uuid);
        mainPage.remove(uuid);
        setupSearch.remove(uuid);
        setupPage.remove(uuid);
    }

    public static void clearAllPlayerStates() {
        mainSearch.clear();
        mainPage.clear();
        setupSearch.clear();
        setupPage.clear();
    }

    // ---------- 主界面打开 ----------
    public static void openMainMenu(Player player, WarpManager wm, int page, WarpGUIPlugin plugin) {
        UUID uuid = player.getUniqueId();
        String search = mainSearch.getOrDefault(uuid, null);
        List<WarpData> filtered = new ArrayList<>();
        for (WarpData w : wm.getWarps()) {
            if (!w.isPublic() && !w.getCreatorUuid().equals(uuid) && !player.hasPermission("warpgui.gui.admin"))
                continue;
            if (search != null && !search.isEmpty() && !w.getName().toLowerCase().contains(search.toLowerCase()))
                continue;
            filtered.add(w);
        }

        int total = Math.max(1, (int) Math.ceil((double) filtered.size() / ITEMS_PER_PAGE));
        page = Math.max(1, Math.min(page, total));
        mainPage.put(uuid, page);

        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, getGuiCfg(plugin, "main-menu-title"));
        setBorder(inv);
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filtered.size());
        List<WarpData> pageItems = filtered.subList(start, end);
        int[] slots = getContentSlots();
        for (int i = 0; i < pageItems.size(); i++) {
            WarpData warp = pageItems.get(i);
            Material mat = Material.matchMaterial(warp.getIconMaterial());
            if (mat == null) mat = Material.GRASS_BLOCK;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MM.deserialize("<gold>" + warp.getName() + "</gold>"));
            meta.lore(Arrays.asList(
                    MM.deserialize(getGuiCfg(plugin, "warp-lore-creator").replace("{creator}", warp.getCreatorName())),
                    MM.deserialize(getGuiCfg(plugin, "warp-lore-coords")
                            .replace("{x}", String.format("%.1f", warp.getLocation().getX()))
                            .replace("{y}", String.format("%.1f", warp.getLocation().getY()))
                            .replace("{z}", String.format("%.1f", warp.getLocation().getZ()))),
                    MM.deserialize(getGuiCfg(plugin, "warp-lore-world").replace("{world}", warp.getLocation().getWorld().getName()))
            ));
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        // 工具栏
        inv.setItem(45, createGuiItem(plugin, Material.COMPASS, "search-item-name", "search-item-lore"));
        inv.setItem(49, createGuiItem(plugin, Material.NETHER_STAR, "create-item-name", "create-item-lore"));
        inv.setItem(53, createGuiItem(plugin, Material.WRITABLE_BOOK, "setup-item-name", "setup-item-lore"));

        // 翻页按钮 (slot 36, 44)
        if (total > 1) {
            inv.setItem(36, createPageButton(plugin, true, page, total));
            inv.setItem(44, createPageButton(plugin, false, page, total));
        } else {
            inv.setItem(36, createPageIndicator(plugin, page, total));
        }

        player.openInventory(inv);
    }

    // 无 page 参数重载（外部调用，默认 page=1）
    public static void openMainMenu(Player player, WarpManager wm, WarpGUIPlugin plugin) {
        openMainMenu(player, wm, 1, plugin);
    }

    // ---------- 设置界面打开 ----------
    public static void openSetupMenu(Player player, WarpManager wm, WarpGUIPlugin plugin, int page) {
        UUID uuid = player.getUniqueId();
        String search = setupSearch.getOrDefault(uuid, null);
        List<WarpData> own = new ArrayList<>();
        for (WarpData w : wm.getWarps()) {
            if (!w.getCreatorUuid().equals(uuid) && !player.hasPermission("warpgui.gui.admin")) continue;
            if (search != null && !search.isEmpty() && !w.getName().toLowerCase().contains(search.toLowerCase()))
                continue;
            own.add(w);
        }

        int total = Math.max(1, (int) Math.ceil((double) own.size() / ITEMS_PER_PAGE));
        page = Math.max(1, Math.min(page, total));
        setupPage.put(uuid, page);

        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, getGuiCfg(plugin, "setup-menu-title"));
        setBorder(inv);
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, own.size());
        List<WarpData> pageItems = own.subList(start, end);
        int[] slots = getContentSlots();
        for (int i = 0; i < pageItems.size(); i++) {
            WarpData warp = pageItems.get(i);
            Material mat = Material.matchMaterial(warp.getIconMaterial());
            ItemStack item = new ItemStack(mat != null ? mat : Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MM.deserialize("<yellow>" + warp.getName() + "</yellow>"));
            meta.lore(Arrays.asList(
                    MM.deserialize("<gray>状态: " + (warp.isPublic() ? getGuiCfg(plugin, "public-status") : getGuiCfg(plugin, "private-status")) + "</gray>"),
                    MM.deserialize(getGuiCfg(plugin, "toggle-hint")),
                    MM.deserialize(getGuiCfg(plugin, "delete-hint").replace("{name}", warp.getName()))
            ));
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        // 工具栏：搜索（自己）、返回
        inv.setItem(45, createGuiItem(plugin, Material.COMPASS, "setup-search-item-name", "setup-search-item-lore"));
        inv.setItem(53, createGuiItem(plugin, Material.BARRIER, "back-item-name", "back-item-lore"));

        if (total > 1) {
            inv.setItem(36, createPageButton(plugin, true, page, total));
            inv.setItem(44, createPageButton(plugin, false, page, total));
        } else {
            inv.setItem(36, createPageIndicator(plugin, page, total));
        }

        player.openInventory(inv);
    }

    public static void openSetupMenu(Player player, WarpManager wm, WarpGUIPlugin plugin) {
        openSetupMenu(player, wm, plugin, 1);
    }

    // ---------- 搜索对话框（全局）----------
    public static void openSearchDialog(Player player, WarpGUIPlugin plugin) {
        Dialog dialog = Dialog.create(b -> b.empty()
                .base(DialogBase.builder(MM.deserialize("<gold>" + getDialogCfg(plugin, "search-title") + "</gold>"))
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(MM.deserialize("<gray>" + getDialogCfg(plugin, "search-desc") + "</gray>"), 300)))
                        .inputs(List.of(DialogInput.text("search_name", MM.deserialize("<yellow>" + getDialogCfg(plugin, "search-input-label") + "</yellow>")).build()))
                        .build()
                )
                .type(DialogType.multiAction(List.of(
                        ActionButton.create(MM.deserialize("<green>" + getDialogCfg(plugin, "search-button") + "</green>"),
                                MM.deserialize("<gray>" + getDialogCfg(plugin, "search-button-desc") + "</gray>"), 50,
                                DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String text = view.getText("search_name");
                                        if (text != null && !text.isEmpty()) {
                                            mainSearch.put(p.getUniqueId(), text);
                                        } else {
                                            mainSearch.remove(p.getUniqueId());
                                        }
                                        mainPage.remove(p.getUniqueId());
                                        openMainMenu(p, WarpGUIPlugin.getInstance().warpManager, 1, plugin);
                                    }
                                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(5)).build())
                        ),
                        ActionButton.create(MM.deserialize("<red>" + getDialogCfg(plugin, "cancel-button") + "</red>"),
                                MM.deserialize("<gray>" + getDialogCfg(plugin, "cancel-desc") + "</gray>"), 50,
                                DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        openMainMenu(p, WarpGUIPlugin.getInstance().warpManager, 1, plugin);
                                    }
                                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(5)).build())
                        )
                ), null, 2)));
        player.showDialog(dialog);
    }

    // ---------- 设置搜索对话框 ----------
    public static void openSetupSearchDialog(Player player, WarpGUIPlugin plugin) {
        Dialog dialog = Dialog.create(b -> b.empty()
                .base(DialogBase.builder(MM.deserialize("<gold>" + getDialogCfg(plugin, "setup-search-title") + "</gold>"))
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(MM.deserialize("<gray>" + getDialogCfg(plugin, "setup-search-desc") + "</gray>"), 300)))
                        .inputs(List.of(DialogInput.text("search_name", MM.deserialize("<yellow>" + getDialogCfg(plugin, "search-input-label") + "</yellow>")).build()))
                        .build()
                )
                .type(DialogType.multiAction(List.of(
                        ActionButton.create(MM.deserialize("<green>" + getDialogCfg(plugin, "search-button") + "</green>"),
                                MM.deserialize("<gray>" + getDialogCfg(plugin, "setup-search-button-desc") + "</gray>"), 50,
                                DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String text = view.getText("search_name");
                                        if (text != null && !text.isEmpty()) {
                                            setupSearch.put(p.getUniqueId(), text);
                                        } else {
                                            setupSearch.remove(p.getUniqueId());
                                        }
                                        setupPage.remove(p.getUniqueId());
                                        openSetupMenu(p, WarpGUIPlugin.getInstance().warpManager, plugin, 1);
                                    }
                                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(5)).build())
                        ),
                        ActionButton.create(MM.deserialize("<red>" + getDialogCfg(plugin, "cancel-button") + "</red>"),
                                MM.deserialize("<gray>" + getDialogCfg(plugin, "cancel-desc") + "</gray>"), 50,
                                DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        openSetupMenu(p, WarpGUIPlugin.getInstance().warpManager, plugin, 1);
                                    }
                                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(5)).build())
                        )
                ), null, 2)));
        player.showDialog(dialog);
    }

    // ---------- 创建对话框 ----------
    public static void openCreateDialog(Player player, WarpGUIPlugin plugin) {
        Dialog dialog = Dialog.create(b -> b.empty()
                .base(DialogBase.builder(MM.deserialize("<green>" + getDialogCfg(plugin, "create-title") + "</green>"))
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(MM.deserialize("<dark_gray>" + getDialogCfg(plugin, "create-icon-hint") + "</dark_gray>"), 200)))
                        .inputs(List.of(
                                DialogInput.text("warp_name", MM.deserialize("<yellow>" + getDialogCfg(plugin, "create-name-label") + "</yellow>")).build(),
                                DialogInput.text("warp_icon", MM.deserialize("<yellow>" + getDialogCfg(plugin, "create-icon-label") + "</yellow>")).build(),
                                DialogInput.bool("warp_public", MM.deserialize("<yellow>" + getDialogCfg(plugin, "create-public-label") + "</yellow>")).build()
                        ))
                        .build()
                )
                .type(DialogType.multiAction(List.of(
                        ActionButton.create(MM.deserialize("<green>" + getDialogCfg(plugin, "create-confirm-button") + "</green>"),
                                MM.deserialize("<gray>" + getDialogCfg(plugin, "create-confirm-desc") + "</gray>"), 50,
                                DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String name = view.getText("warp_name");
                                        String icon = view.getText("warp_icon");
                                        Boolean isPublic = view.getBoolean("warp_public");
                                        if (name == null || name.isEmpty()) {
                                            p.sendMessage(MM.deserialize(getMsg(plugin, "name-empty")));
                                            return;
                                        }
                                        WarpManager wm = WarpGUIPlugin.getInstance().warpManager;
                                        if (wm.getWarp(name) != null) {
                                            p.sendMessage(MM.deserialize(getMsg(plugin, "warp-name-exists")));
                                            return;
                                        }
                                        String iconMat = "GRASS_BLOCK";
                                        LangManager lang = WarpGUIPlugin.getInstance().getLangManager();
                                        if (icon != null && !icon.isEmpty()) {
                                            Material m = lang.matchMaterial(icon);
                                            if (m != null && m.isItem()) iconMat = m.name();
                                            else p.sendMessage(MM.deserialize(getMsg(plugin, "invalid-material")));
                                        }
                                        boolean pub = isPublic != null ? isPublic : true;
                                        WarpData newWarp = new WarpData(name, p.getUniqueId(), p.getName(), p.getLocation(), iconMat, pub);
                                        wm.createWarp(newWarp);
                                        Material created = Material.matchMaterial(iconMat);
                                        String dName = created != null ? lang.getDisplayName(created) : iconMat;
                                        p.sendMessage(MM.deserialize(getMsg(plugin, "warp-created")
                                                .replace("{name}", name).replace("{icon}", dName)
                                                .replace("{public}", pub ? getGuiCfg(plugin, "public-status") : getGuiCfg(plugin, "private-status"))));
                                        // 返回主界面，清除搜索状态
                                        mainSearch.remove(p.getUniqueId());
                                        openMainMenu(p, wm, 1, plugin);
                                    }
                                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(5)).build())
                        ),
                        ActionButton.create(MM.deserialize("<red>" + getDialogCfg(plugin, "cancel-button") + "</red>"),
                                MM.deserialize("<gray>" + getDialogCfg(plugin, "cancel-desc") + "</gray>"), 50,
                                DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openMainMenu(p, WarpGUIPlugin.getInstance().warpManager, 1, plugin);
                                }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(5)).build())
                        )
                ), null, 2)));
        player.showDialog(dialog);
    }

    // ---------- 事件处理 ----------
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= GUI_SIZE) return;
        e.setCancelled(true);

        if (title.equals(getGuiCfg(plugin, "main-menu-title"))) {
            if (isContentSlot(slot)) {
                ItemStack clicked = e.getCurrentItem();
                if (clicked == null || !clicked.hasItemMeta() || clicked.getItemMeta().displayName() == null) return;
                String name = MM.serialize(clicked.getItemMeta().displayName()).replace("<gold>","").replace("</gold>","").trim();
                p.performCommand("warpgui tp " + name);
                p.closeInventory();
            } else if (slot == 36) {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {
                    int cur = mainPage.getOrDefault(p.getUniqueId(), 1);
                    if (cur > 1) openMainMenu(p, warpManager, cur - 1, plugin);
                }
            } else if (slot == 44) {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {
                    int cur = mainPage.getOrDefault(p.getUniqueId(), 1);
                    int total = getTotalFromItem(e.getCurrentItem());
                    if (cur < total) openMainMenu(p, warpManager, cur + 1, plugin);
                }
            } else if (slot == 45) openSearchDialog(p, plugin);
            else if (slot == 49) {
                if (!p.hasPermission("warpgui.gui.create")) {
                    p.sendMessage(MM.deserialize(getMsg(plugin, "no-create-permission")));
                    return;
                }
                openCreateDialog(p, plugin);
            } else if (slot == 53) openSetupMenu(p, warpManager, plugin, 1);
        }
        else if (title.equals(getGuiCfg(plugin, "setup-menu-title"))) {
            if (isContentSlot(slot)) {
                ItemStack clicked = e.getCurrentItem();
                if (clicked == null || !clicked.hasItemMeta() || clicked.getItemMeta().displayName() == null) return;
                String name = MM.serialize(clicked.getItemMeta().displayName()).replace("<yellow>","").replace("</yellow>","").trim();
                WarpData warp = warpManager.getWarp(name);
                if (warp == null) return;
                if (e.isLeftClick()) {
                    warp.setPublic(!warp.isPublic());
                    warpManager.updateWarp(warp);
                    p.sendMessage(MM.deserialize(getMsg(plugin, "public-toggled")
                            .replace("{name}", warp.getName())
                            .replace("{status}", warp.isPublic() ? getGuiCfg(plugin, "public-status") : getGuiCfg(plugin, "private-status"))));
                    int cur = setupPage.getOrDefault(p.getUniqueId(), 1);
                    openSetupMenu(p, warpManager, plugin, cur);
                }
            } else if (slot == 36) {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {
                    int cur = setupPage.getOrDefault(p.getUniqueId(), 1);
                    if (cur > 1) openSetupMenu(p, warpManager, plugin, cur - 1);
                }
            } else if (slot == 44) {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {
                    int cur = setupPage.getOrDefault(p.getUniqueId(), 1);
                    int total = getTotalFromItem(e.getCurrentItem());
                    if (cur < total) openSetupMenu(p, warpManager, plugin, cur + 1);
                }
            } else if (slot == 45) openSetupSearchDialog(p, plugin);
            else if (slot == 53) {
                // 返回主菜单，清除设置搜索状态
                setupSearch.remove(p.getUniqueId());
                openMainMenu(p, warpManager, 1, plugin);
            }
        }
    }

    // ---------- UI 工具 ----------
    private void setBorder(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.displayName(Component.empty());
        glass.setItemMeta(gm);
        for (int i = 0; i < 9; i++) inv.setItem(i, glass); // 顶部
        for (int row = 1; row <= 4; row++) {
            inv.setItem(row * 9, glass);
            inv.setItem(row * 9 + 8, glass);
        }
    }

    private int[] getContentSlots() {
        int[] slots = new int[28];
        int idx = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[idx++] = row * 9 + col;
        return slots;
    }

    private boolean isContentSlot(int slot) {
        int row = slot / 9, col = slot % 9;
        return row >= 1 && row <= 4 && col >= 1 && col <= 7;
    }

    private ItemStack createGuiItem(WarpGUIPlugin plugin, Material mat, String nameKey, String loreKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MM.deserialize(getGuiCfg(plugin, nameKey)));
        meta.lore(Arrays.asList(MM.deserialize(getGuiCfg(plugin, loreKey))));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageButton(WarpGUIPlugin plugin, boolean prev, int page, int total) {
        Material mat = Material.ARROW;
        String nameKey = prev ? "previous-page-name" : "next-page-name";
        String loreKey = prev ? "previous-page-lore" : "next-page-lore";
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MM.deserialize(getGuiCfg(plugin, nameKey)
                .replace("{current}", String.valueOf(page))
                .replace("{total}", String.valueOf(total))));
        meta.lore(Arrays.asList(MM.deserialize(getGuiCfg(plugin, loreKey))));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageIndicator(WarpGUIPlugin plugin, int page, int total) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MM.deserialize(getGuiCfg(plugin, "page-info-name")
                .replace("{current}", String.valueOf(page))
                .replace("{total}", String.valueOf(total))));
        meta.lore(Collections.emptyList());
        item.setItemMeta(meta);
        return item;
    }

    private int getTotalFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().displayName() == null) return 1;
        String name = MM.serialize(item.getItemMeta().displayName());
        for (String part : name.split(" ")) {
            if (part.contains("/")) {
                try { return Integer.parseInt(part.split("/")[1]); } catch (NumberFormatException ignored) {}
            }
        }
        return 1;
    }

    // 配置快捷读取
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