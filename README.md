# WarpGUI

一个基于 Paper 的传送点 GUI 插件，支持中英文图标匹配、SQLite/MySQL 数据库、Dialog API 交互

Minecraft 1.21.11 Paper API Java 21 MIT

## 功能

+   **GUI 界面** — 使用 `/warpgui` 打开传送点主界面，浏览所有公开传送点
+   **搜索** — 通过 Paper Dialog API 按名称模糊搜索传送点
+   **创建向导** — 使用 Dialog 输入名称、图标（支持中英文）、公开状态，自动记录当前坐标
+   **图标匹配** — 支持中文名（通过语言文件）、英文枚举名、`minecraft:xxx` 格式
+   **传送点管理** — 在设置界面切换公开/私有状态，通过命令删除
+   **快速传送** — `/warpgui tp <名称>` 直接传送
+   **双数据库** — 支持 SQLite（默认）和 MySQL，自动建表
+   **Tab 补全** — 子命令和传送点名称自动补全
+   **可配置消息** — 所有提示文本均可在 `config.yml` 中自定义

## 命令

| 命令 | 说明 |
| --- | --- |
| `/warpgui` | 打开传送点 GUI 界面 |
| `/warpgui help` | 显示帮助信息 |
| `/warpgui tp <名称>` | 传送到指定传送点 |
| `/warpgui delete <名称>` | 删除传送点 |
| `/warpgui reload` | 重载配置与数据库 |

## 权限

| 权限节点 | 说明 | 默认 |
| --- | --- | --- |
| `warpgui.gui.use` | 使用 GUI 和传送 | 所有人 |
| `warpgui.gui.create` | 创建传送点 | 所有人 |
| `warpgui.gui.delete` | 删除自己的传送点 | 所有人 |
| `warpgui.gui.admin` | 管理所有传送点 | OP |

## 安装

1.  从 [Releases](https://github.com/ahhhub/WarpGUI/releases) 下载最新版 `WarpGUI-*.jar`
2.  放入服务器 `plugins/` 文件夹
3.  启动服务器，插件会自动生成默认配置文件
4.  （可选）编辑 `plugins/WarpGUI/config.yml` 切换数据库或自定义消息
5.  （可选）编辑 `plugins/WarpGUI/lang/zh-cn.yml` 自定义中文图标名映射

## 构建

```
git clone https://github.com/ahhhub/WarpGUI.git
cd WarpGUI
mvn clean package -DskipTests
```

输出位于 `target/WarpGUI-*.jar`

## 项目结构

```
src/main/
├── java/com/warpgui/main/
│   ├── WarpGUIPlugin.java      # 插件主类
│   ├── DatabaseManager.java    # 数据库管理（SQLite / MySQL）
│   ├── WarpManager.java        # 传送点缓存管理
│   ├── WarpCommand.java        # 命令处理与 Tab 补全
│   ├── MenuListener.java       # GUI 与 Dialog 事件
│   ├── WarpData.java           # 传送点数据模型
│   └── LangManager.java        # 语言文件与图标映射
└── resources/
    ├── plugin.yml
    ├── config.yml
    └── lang/
        └── zh-cn.yml
```

## 许可证

本项目基于 [MIT License](LICENSE) 开源。

Made by 未定awa · [GitHub](https://github.com/ahhhub/WarpGUI)