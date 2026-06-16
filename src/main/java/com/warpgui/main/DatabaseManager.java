package com.warpgui.main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final WarpGUIPlugin plugin;
    private Connection connection;

    public DatabaseManager(WarpGUIPlugin plugin) {
        this.plugin = plugin;
    }

    public void initDatabase() {
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type", "sqlite").toLowerCase();

        try {
            if (type.equals("mysql")) {
                String host = config.getString("database.host");
                int port = config.getInt("database.port");
                String name = config.getString("database.name");
                String user = config.getString("database.user");
                String password = config.getString("database.password");
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=false&autoReconnect=true",
                        user, password);
            } else {
                File dataFolder = new File(plugin.getDataFolder(), "warps.db");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath());
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS warps (" +
                        "name VARCHAR(64) PRIMARY KEY, " +
                        "creator_uuid VARCHAR(36), " +
                        "creator_name VARCHAR(16), " +
                        "world VARCHAR(64), " +
                        "x DOUBLE, y DOUBLE, z DOUBLE, " +
                        "yaw FLOAT, pitch FLOAT, " +
                        "icon VARCHAR(64), " +
                        "is_public BOOLEAN)");
            }
        } catch (SQLException e) {
            // silently fail
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // silently fail
        }
    }

    public List<WarpData> loadAllWarps() {
        List<WarpData> warps = new ArrayList<>();
        if (!isConnected()) return warps;

        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM warps");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String worldName = rs.getString("world");
                if (Bukkit.getWorld(worldName) == null) {
                    continue;
                }
                Location loc = new Location(
                        Bukkit.getWorld(worldName),
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch")
                );
                WarpData warp = new WarpData(
                        rs.getString("name"),
                        UUID.fromString(rs.getString("creator_uuid")),
                        rs.getString("creator_name"),
                        loc,
                        rs.getString("icon"),
                        rs.getBoolean("is_public")
                );
                warps.add(warp);
            }
        } catch (SQLException e) {
            // silently fail
        }
        return warps;
    }

    public void saveWarp(WarpData warp) {
        if (!isConnected()) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO warps (name, creator_uuid, creator_name, world, x, y, z, yaw, pitch, icon, is_public) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )) {
            ps.setString(1, warp.getName());
            ps.setString(2, warp.getCreatorUuid().toString());
            ps.setString(3, warp.getCreatorName());
            ps.setString(4, warp.getLocation().getWorld().getName());
            ps.setDouble(5, warp.getLocation().getX());
            ps.setDouble(6, warp.getLocation().getY());
            ps.setDouble(7, warp.getLocation().getZ());
            ps.setFloat(8, warp.getLocation().getYaw());
            ps.setFloat(9, warp.getLocation().getPitch());
            ps.setString(10, warp.getIconMaterial());
            ps.setBoolean(11, warp.isPublic());
            ps.executeUpdate();
        } catch (SQLException e) {
            // silently fail
        }
    }

    public void deleteWarp(String name) {
        if (!isConnected()) return;

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM warps WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            // silently fail
        }
    }
}