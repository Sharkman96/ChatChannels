package com.example.chatchannels.server.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatPlayerDataStorage extends SavedData {

    private static final String NAME = "chatchannels_players";

    private final Map<UUID, ChatPlayerData> dataMap = new HashMap<>();

    public static ChatPlayerDataStorage get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ChatPlayerDataStorage::load, ChatPlayerDataStorage::new, NAME);
    }

    public ChatPlayerDataStorage() {
    }

    public static ChatPlayerDataStorage load(CompoundTag tag) {
        ChatPlayerDataStorage storage = new ChatPlayerDataStorage();
        CompoundTag players = tag.getCompound("Players");
        for (String key : players.getAllKeys()) {
            try {
                UUID id = UUID.fromString(key);
                ChatPlayerData data = ChatPlayerData.load(players.getCompound(key));
                storage.dataMap.put(id, data);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return storage;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag players = new CompoundTag();
        for (Map.Entry<UUID, ChatPlayerData> e : dataMap.entrySet()) {
            players.put(e.getKey().toString(), e.getValue().save());
        }
        tag.put("Players", players);
        return tag;
    }

    public ChatPlayerData getOrCreate(UUID id) {
        return dataMap.computeIfAbsent(id, u -> new ChatPlayerData());
    }

    public void markDirty() {
        setDirty();
    }
}
