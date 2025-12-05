package com.example.chatchannels.server.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatPlayerData {

    private String currentChannelId;
    private final Set<UUID> ignoredPlayers = new HashSet<>();
    private final Set<String> mutedChannels = new HashSet<>();

    public String getCurrentChannelId() {
        return currentChannelId;
    }

    public void setCurrentChannelId(String currentChannelId) {
        this.currentChannelId = currentChannelId;
    }

    public Set<UUID> getIgnoredPlayers() {
        return ignoredPlayers;
    }

    public Set<String> getMutedChannels() {
        return mutedChannels;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (currentChannelId != null) {
            tag.putString("CurrentChannel", currentChannelId);
        }
        ListTag ignored = new ListTag();
        for (UUID id : ignoredPlayers) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Id", id);
            ignored.add(t);
        }
        tag.put("IgnoredPlayers", ignored);

        ListTag muted = new ListTag();
        for (String ch : mutedChannels) {
            CompoundTag t = new CompoundTag();
            t.putString("Id", ch);
            muted.add(t);
        }
        tag.put("MutedChannels", muted);

        return tag;
    }

    public static ChatPlayerData load(CompoundTag tag) {
        ChatPlayerData data = new ChatPlayerData();
        if (tag.contains("CurrentChannel", Tag.TAG_STRING)) {
            data.currentChannelId = tag.getString("CurrentChannel");
        }
        ListTag ignored = tag.getList("IgnoredPlayers", Tag.TAG_COMPOUND);
        for (Tag t : ignored) {
            if (t instanceof CompoundTag ct && ct.hasUUID("Id")) {
                data.ignoredPlayers.add(ct.getUUID("Id"));
            }
        }
        ListTag muted = tag.getList("MutedChannels", Tag.TAG_COMPOUND);
        for (Tag t : muted) {
            if (t instanceof CompoundTag ct && ct.contains("Id", Tag.TAG_STRING)) {
                data.mutedChannels.add(ct.getString("Id"));
            }
        }
        return data;
    }
}
