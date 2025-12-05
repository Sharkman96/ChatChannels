package com.example.chatchannels.server.chat;

import com.example.chatchannels.api.ChannelDefinition;
import com.example.chatchannels.network.NetworkHandler;
import com.example.chatchannels.network.S2CCurrentChannelPacket;
import com.example.chatchannels.server.channel.ChannelManager;
import com.example.chatchannels.server.perm.PermissionManager;
import com.example.chatchannels.server.player.PlayerChatStateManager;
import com.example.chatchannels.server.ratelimit.RateLimiterManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public final class ServerChatHandler {

    private ServerChatHandler() {
    }

    public static void handleChannelMessage(ServerPlayer player, String channelId, String message) {
        if (player == null) {
            return;
        }

        String raw = message == null ? "" : message.trim();
        if (raw.isEmpty()) {
            return;
        }

        if (raw.length() > 256) {
            raw = raw.substring(0, 256);
        }
        String sanitized = raw.replaceAll("\r", "").replaceAll("\n", " ");
        if (sanitized.isEmpty()) {
            return;
        }

        if (!RateLimiterManager.getInstance().tryConsume(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You are sending messages too fast."));
            return;
        }

        ChannelManager channelManager = ChannelManager.getInstance();
        String targetChannelId = channelId;
        if (targetChannelId == null || targetChannelId.isEmpty()) {
            targetChannelId = PlayerChatStateManager.getInstance()
                    .getCurrentChannel(player, channelManager.getDefaultChannelId());
        }

        ChannelDefinition channel = channelManager.getChannel(targetChannelId);
        if (channel == null) {
            targetChannelId = channelManager.getDefaultChannelId();
            channel = channelManager.getChannel(targetChannelId);
            if (channel == null) {
                return;
            }
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty()) {
            if (!PermissionManager.hasPermission(player, permission)) {
                player.sendSystemMessage(Component.literal("You don't have permission to talk in this channel."));
                return;
            }
        }

        PlayerChatStateManager.getInstance().setCurrentChannel(player, targetChannelId);

        // синхронизируем текущий канал с клиентом
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CCurrentChannelPacket(targetChannelId));

        // TODO: проверки прав и игнор-листов
        ChatMessageRouter.sendToChannel(player, channel, sanitized);
    }

    public static void handlePrivateMessage(ServerPlayer sender, ServerPlayer target, String message) {
        if (sender == null || target == null) {
            return;
        }

        String raw = message == null ? "" : message.trim();
        if (raw.isEmpty()) {
            return;
        }

        if (raw.length() > 256) {
            raw = raw.substring(0, 256);
        }
        String sanitized = raw.replaceAll("\r", "").replaceAll("\n", " ");
        if (sanitized.isEmpty()) {
            return;
        }

        if (!RateLimiterManager.getInstance().tryConsume(sender.getUUID())) {
            sender.sendSystemMessage(Component.literal("You are sending messages too fast."));
            return;
        }

        ChannelManager channelManager = ChannelManager.getInstance();
        ChannelDefinition channel = channelManager.getChannel("pm");
        if (channel == null) {
            channel = channelManager.getChannel(channelManager.getDefaultChannelId());
            if (channel == null) {
                return;
            }
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty()) {
            if (!PermissionManager.hasPermission(sender, permission)) {
                sender.sendSystemMessage(Component.literal("You don't have permission to send private messages."));
                return;
            }
        }

        ChatMessageRouter.sendPrivate(sender, target, channel, sanitized);
    }
}
