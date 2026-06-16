# WarpGUI

A Paper-based warp GUI plugin with bilingual icon matching, SQLite/MySQL support, and Dialog API interaction

Minecraft 1.21.11 Paper API Java 21 MIT

## Features

+   **GUI Interface** — Open the warp menu with `/warpgui` and browse all public warps
+   **Search** — Fuzzy-search warps by name via Paper Dialog API
+   **Creation Wizard** — Use Dialog to input name, icon (supports Chinese & English), and visibility; current coordinates are recorded automatically
+   **Icon Matching** — Supports Chinese names (via language file), English enum names, and `minecraft:xxx` format
+   **Warp Management** — Toggle public/private in the settings menu; delete via command
+   **Quick Teleport** — `/warpgui tp <name>` for instant teleport
+   **Dual Database** — SQLite (default) and MySQL support with automatic table creation
+   **Tab Completion** — Subcommands and warp names are auto-completed
+   **Configurable Messages** — All prompts can be customized in `config.yml`

## Commands

| Command | Description |
| --- | --- |
| `/warpgui` | Open the warp GUI |
| `/warpgui help` | Show help |
| `/warpgui tp <name>` | Teleport to a warp |
| `/warpgui delete <name>` | Delete a warp |
| `/warpgui reload` | Reload config and database |

## Permissions

| Node | Description | Default |
| --- | --- | --- |
| `warpgui.gui.use` | Use GUI and teleport | Everyone |
| `warpgui.gui.create` | Create warps | Everyone |
| `warpgui.gui.delete` | Delete own warps | Everyone |
| `warpgui.gui.admin` | Manage all warps | OP |

## Installation

1.  Download the latest `WarpGUI-*.jar` from [Releases](https://github.com/ahhhub/WarpGUI/releases)
2.  Place it in the server's `plugins/` folder
3.  Start the server — default config files will be generated automatically
4.  (Optional) Edit `plugins/WarpGUI/config.yml` to switch database or customize messages
5.  (Optional) Edit `plugins/WarpGUI/lang/zh-cn.yml` to customize Chinese icon name mappings

## Build

```
git clone https://github.com/ahhhub/WarpGUI.git
cd WarpGUI
mvn clean package -DskipTests
```

Output: `target/WarpGUI-*.jar`

## Project Structure

```
src/main/
├── java/com/warpgui/main/
│   ├── WarpGUIPlugin.java      # Plugin main class
│   ├── DatabaseManager.java    # Database (SQLite / MySQL)
│   ├── WarpManager.java        # Warp cache manager
│   ├── WarpCommand.java        # Command & tab completion
│   ├── MenuListener.java       # GUI & Dialog events
│   ├── WarpData.java           # Warp data model
│   └── LangManager.java        # Language file & icon mapping
└── resources/
    ├── plugin.yml
    ├── config.yml
    └── lang/
        └── zh-cn.yml
```

## License

This project is open-sourced under the [MIT License](LICENSE).

Made by 未定awa · [GitHub](https://github.com/ahhhub/WarpGUI)