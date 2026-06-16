package com.warpgui.main;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理语言文件，提供中文名 -> Material 的反向映射
 */
public class LangManager {

    private final WarpGUIPlugin plugin;
    private final Map<String, Material> nameToMaterial = new HashMap<>();
    private String langCode;

    public LangManager(WarpGUIPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载语言文件并构建反向映射
     * 语言文件路径: plugins/WarpGUI/lang/<lang>.yml
     */
    public void loadLang() {
        nameToMaterial.clear();
        langCode = plugin.getConfig().getString("lang", "zh-cn");

        File langFile = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");

        if (!langFile.exists()) {
            return;
        }

        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);

        for (String key : langConfig.getKeys(false)) {
            String rawValue = langConfig.getString(key);
            if (rawValue == null || rawValue.isEmpty()) continue;

            String cleanValue = stripColorCodes(rawValue).trim();
            if (cleanValue.isEmpty()) continue;

            Material mat = Material.matchMaterial(key.toUpperCase());
            if (mat == null) continue;

            nameToMaterial.put(cleanValue.toLowerCase(), mat);
            if (mat.isItem()) {
                nameToMaterial.put(mat.name().toLowerCase(), mat);
            }
        }
    }

    /**
     * 根据输入文本匹配 Material
     * 支持: 英文枚举名、带命名空间名、中文名
     */
    public Material matchMaterial(String input) {
        if (input == null || input.isEmpty()) return null;

        // 1. 直接匹配 Material 枚举名
        String upperInput = input.toUpperCase().replace(" ", "_");
        Material direct = Material.matchMaterial(upperInput);
        if (direct != null && direct.isItem()) return direct;

        // 2. 去除颜色代码后匹配中文名
        String cleanInput = stripColorCodes(input).trim().toLowerCase();
        Material fromLang = nameToMaterial.get(cleanInput);
        if (fromLang != null) return fromLang;

        // 3. 模糊匹配
        for (Map.Entry<String, Material> entry : nameToMaterial.entrySet()) {
            if (cleanInput.contains(entry.getKey()) || entry.getKey().contains(cleanInput)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 根据 Material 获取中文显示名
     */
    public String getDisplayName(Material mat) {
        for (Map.Entry<String, Material> entry : nameToMaterial.entrySet()) {
            if (entry.getValue() == mat) {
                return entry.getKey();
            }
        }
        return formatMaterialName(mat);
    }

    /**
     * 去除 Minecraft 颜色代码
     */
    public static String stripColorCodes(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "")
                .replaceAll("&[0-9a-fk-or]", "");
    }

    /**
     * 格式化 Material 名称为可读形式
     */
    private String formatMaterialName(Material mat) {
        String name = mat.name().replace("_", " ").toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}