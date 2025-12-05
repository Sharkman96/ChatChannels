package com.example.chatchannels.api;

import com.google.gson.annotations.SerializedName;

public class ChannelDefinition {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private ChannelType type;

    @SerializedName("prefix")
    private String prefix;

    @SerializedName("color")
    private String colorHex;

    @SerializedName("radius")
    private int radius;

    @SerializedName("default")
    private boolean isDefault;

    @SerializedName("permission")
    private String permission;

    public ChannelDefinition() {
    }

    public ChannelDefinition(String id,
                             String name,
                             ChannelType type,
                             String prefix,
                             String colorHex,
                             int radius,
                             boolean isDefault,
                             String permission) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.prefix = prefix;
        this.colorHex = colorHex;
        this.radius = radius;
        this.isDefault = isDefault;
        this.permission = permission;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ChannelType getType() {
        return type == null ? ChannelType.CUSTOM : type;
    }

    public String getPrefix() {
        return prefix == null ? "" : prefix;
    }

    public String getColorHex() {
        return colorHex == null ? "#FFFFFF" : colorHex;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getPermission() {
        return permission;
    }
}
