package com.example.chatchannels.api;

public class ChannelDefinitionBuilder {

    private String id;
    private String name;
    private ChannelType type = ChannelType.CUSTOM;
    private String prefix;
    private String colorHex;
    private int radius;
    private boolean isDefault;
    private String permission;

    public ChannelDefinitionBuilder id(String id) {
        this.id = id;
        return this;
    }

    public ChannelDefinitionBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ChannelDefinitionBuilder type(ChannelType type) {
        this.type = type;
        return this;
    }

    public ChannelDefinitionBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ChannelDefinitionBuilder color(String colorHex) {
        this.colorHex = colorHex;
        return this;
    }

    public ChannelDefinitionBuilder radius(int radius) {
        this.radius = radius;
        return this;
    }

    public ChannelDefinitionBuilder isDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public ChannelDefinitionBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public ChannelDefinition build() {
        return new ChannelDefinition(id, name, type, prefix, colorHex, radius, isDefault, permission);
    }
}
