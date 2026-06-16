package com.warpgui.main;

import org.bukkit.Location;
import java.util.UUID;

public class WarpData {
    private final String name;
    private final UUID creatorUuid;
    private final String creatorName;
    private final Location location;
    private String iconMaterial;
    private boolean isPublic;

    public WarpData(String name, UUID creatorUuid, String creatorName, Location location, String iconMaterial, boolean isPublic) {
        this.name = name;
        this.creatorUuid = creatorUuid;
        this.creatorName = creatorName;
        this.location = location;
        this.iconMaterial = iconMaterial;
        this.isPublic = isPublic;
    }

    public String getName() {
        return name;
    }

    public UUID getCreatorUuid() {
        return creatorUuid;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public Location getLocation() {
        return location;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public void setIconMaterial(String iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}