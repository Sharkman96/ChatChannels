package com.example.chatchannels.server.player;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerChatStateManager {

    private static final PlayerChatStateManager INSTANCE = new PlayerChatStateManager();

    private PlayerChatStateManager() {
    }

    public static PlayerChatStateManager getInstance() {
        return INSTANCE;
    }

    public String getCurrentChannel(ServerPlayer player, String defaultChannelId) {
        ChatPlayerDataStorage storage = ChatPlayerDataStorage.get(player.serverLevel());
        ChatPlayerData data = storage.getOrCreate(player.getUUID());
        String id = data.getCurrentChannelId();
        return id != null ? id : defaultChannelId;
    }

    public void setCurrentChannel(ServerPlayer player, String channelId) {
        ChatPlayerDataStorage storage = ChatPlayerDataStorage.get(player.serverLevel());
        ChatPlayerData data = storage.getOrCreate(player.getUUID());
        data.setCurrentChannelId(channelId);
        storage.markDirty();
    }

    public void clear(ServerPlayer player) {
        ChatPlayerDataStorage storage = ChatPlayerDataStorage.get(player.serverLevel());
        ChatPlayerData data = storage.getOrCreate(player.getUUID());
        data.setCurrentChannelId(null);
        storage.markDirty();
    }
}
