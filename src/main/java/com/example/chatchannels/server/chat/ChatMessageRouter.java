package com.example.chatchannels.server.chat;

import com.example.chatchannels.api.ChannelDefinition;
import com.example.chatchannels.config.PrivilegeColorConfigLoader;
import com.example.chatchannels.network.NetworkHandler;
import com.example.chatchannels.network.S2CNewMessagePacket;
import com.example.chatchannels.server.log.ChatLogManager;
import com.example.chatchannels.server.perm.ChatPrivilegeFormatter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import com.example.chatchannels.server.player.ChatPlayerData;
import com.example.chatchannels.server.player.ChatPlayerDataStorage;

public final class ChatMessageRouter {

    private ChatMessageRouter() {
    }

    public static void sendToChannel(ServerPlayer sender, ChannelDefinition channel, String message) {
        if (channel == null) {
            return;
        }

        ChatLogManager.log(channel.getId(), sender.getGameProfile().getName(), message);

        switch (channel.getType()) {
            case LOCAL -> sendLocal(sender, channel, message);
            case GLOBAL, TRADE, STAFF, CUSTOM, TEAM, PRIVATE -> sendGlobalLike(sender, channel, message);
        }
    }

    public static void sendPrivate(ServerPlayer sender, ServerPlayer target, ChannelDefinition channel, String message) {
        if (sender == null || target == null || channel == null) {
            return;
        }

        ChatLogManager.log(channel.getId(), sender.getGameProfile().getName(), message);

        // Отправляем сообщение отправителю всегда
        sendToPlayer(sender, channel, sender, message);

        // Получатель видит сообщение, если не замьютил канал и не игнорирует отправителя
        if (!target.getUUID().equals(sender.getUUID())) {
            if (shouldDeliver(sender, target, channel)) {
                sendToPlayer(target, channel, sender, message);
            }
        }
    }

    private static void sendGlobalLike(ServerPlayer sender, ChannelDefinition channel, String message) {
        List<ServerPlayer> players = sender.server.getPlayerList().getPlayers();
        for (ServerPlayer target : players) {
            if (shouldDeliver(sender, target, channel)) {
                sendToPlayer(target, channel, sender, message);
            }
        }
    }

    private static void sendLocal(ServerPlayer sender, ChannelDefinition channel, String message) {
        double radius = channel.getRadius();
        if (radius <= 0) {
            sendGlobalLike(sender, channel, message);
            return;
        }
        double radiusSq = radius * radius;
        List<ServerPlayer> players = sender.server.getPlayerList().getPlayers();
        for (ServerPlayer target : players) {
            if (target.level() != sender.level()) {
                continue;
            }
            if (target.position().distanceToSqr(sender.position()) <= radiusSq) {
                if (shouldDeliver(sender, target, channel)) {
                    sendToPlayer(target, channel, sender, message);
                }
            }
        }
    }

    private static boolean shouldDeliver(ServerPlayer sender, ServerPlayer target, ChannelDefinition channel) {
        ChatPlayerDataStorage storage = ChatPlayerDataStorage.get(target.serverLevel());
        ChatPlayerData data = storage.getOrCreate(target.getUUID());

        if (data.getMutedChannels().contains(channel.getId())) {
            return false;
        }
        if (data.getIgnoredPlayers().contains(sender.getUUID())) {
            return false;
        }
        return true;
    }

    private static void sendToPlayer(ServerPlayer target, ChannelDefinition channel, ServerPlayer sender, String message) {
        String privilege = ChatPrivilegeFormatter.getPrivilegeTag(sender);
        int privilegeColor = PrivilegeColorConfigLoader.getColor(privilege);

        S2CNewMessagePacket packet = new S2CNewMessagePacket(
                channel.getId(),
                sender.getUUID(),
                sender.getGameProfile().getName(),
                message,
                privilege,
                privilegeColor
        );
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), packet);
    }
}
