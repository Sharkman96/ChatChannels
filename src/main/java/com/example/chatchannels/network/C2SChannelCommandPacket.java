package com.example.chatchannels.network;

import com.example.chatchannels.api.ChannelDefinition;
import com.example.chatchannels.config.ChatChannelsServerConfig;
import com.example.chatchannels.server.channel.ChannelManager;
import com.example.chatchannels.server.perm.PermissionManager;
import com.example.chatchannels.server.player.PlayerChatStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class C2SChannelCommandPacket {

    public enum Action {
        JOIN,
        LEAVE,
        CREATE,
        LIST
    }

    private final Action action;
    private final String channelId;
    private final String argument;

    public C2SChannelCommandPacket(Action action, String channelId, String argument) {
        this.action = action;
        this.channelId = channelId;
        this.argument = argument;
    }

    public C2SChannelCommandPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);
        this.channelId = buf.readUtf(64);
        this.argument = buf.readUtf(256);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(channelId, 64);
        buf.writeUtf(argument, 256);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            ChannelManager manager = ChannelManager.getInstance();
            switch (action) {
                case LIST -> {
                    List<ChannelDefinition> defs = new ArrayList<>();
                    for (ChannelDefinition def : manager.getChannels()) {
                        String perm = def.getPermission();
                        if (perm != null && !perm.isEmpty()) {
                            if (!PermissionManager.hasPermission(player, perm)) {
                                continue;
                            }
                        }
                        defs.add(def);
                    }
                    S2CChannelListPacket packet = new S2CChannelListPacket(defs);
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
                }
                case JOIN -> {
                    if (channelId == null || channelId.isEmpty()) {
                        return;
                    }
                    ChannelDefinition def = manager.getChannel(channelId);
                    if (def == null) {
                        player.sendSystemMessage(Component.literal("Unknown channel: " + channelId));
                        return;
                    }
                    PlayerChatStateManager.getInstance().setCurrentChannel(player, channelId);
                    if (ChatChannelsServerConfig.SHOW_CHANNEL_SWITCH_MESSAGES.get()) {
                        player.sendSystemMessage(Component.literal("Joined channel " + channelId));
                    }
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CCurrentChannelPacket(channelId));
                }
                case LEAVE -> {
                    if (channelId == null || channelId.isEmpty()) {
                        return;
                    }
                    String current = PlayerChatStateManager.getInstance()
                            .getCurrentChannel(player, manager.getDefaultChannelId());
                    String newId = current;
                    if (channelId.equals(current)) {
                        newId = manager.getDefaultChannelId();
                        PlayerChatStateManager.getInstance().setCurrentChannel(player, newId);
                    }
                    if (ChatChannelsServerConfig.SHOW_CHANNEL_SWITCH_MESSAGES.get()) {
                        player.sendSystemMessage(Component.literal("Left channel " + channelId));
                    }
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CCurrentChannelPacket(newId));
                }
                case CREATE -> {
                    // Будет реализовано позже (временные/пользовательские каналы)
                    player.sendSystemMessage(Component.literal("Channel creation is not implemented yet."));
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public Action getAction() {
        return action;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getArgument() {
        return argument;
    }
}
