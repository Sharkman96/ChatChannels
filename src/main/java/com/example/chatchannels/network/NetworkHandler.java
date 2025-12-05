package com.example.chatchannels.network;

import com.example.chatchannels.ChatChannels;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ChatChannels.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextPacketId = 0;

    private NetworkHandler() {
    }

    public static void init() {
        CHANNEL.registerMessage(
                nextId(),
                C2SSendMessagePacket.class,
                C2SSendMessagePacket::encode,
                C2SSendMessagePacket::new,
                (msg, ctx) -> msg.handle(ctx)
        );

        CHANNEL.registerMessage(
                nextId(),
                S2CNewMessagePacket.class,
                S2CNewMessagePacket::encode,
                S2CNewMessagePacket::new,
                (msg, ctx) -> msg.handle(ctx)
        );

        CHANNEL.registerMessage(
                nextId(),
                S2CChannelListPacket.class,
                S2CChannelListPacket::encode,
                S2CChannelListPacket::new,
                (msg, ctx) -> msg.handle(ctx)
        );

        CHANNEL.registerMessage(
                nextId(),
                C2SChannelCommandPacket.class,
                C2SChannelCommandPacket::encode,
                C2SChannelCommandPacket::new,
                (msg, ctx) -> msg.handle(ctx)
        );

        CHANNEL.registerMessage(
                nextId(),
                S2CCurrentChannelPacket.class,
                S2CCurrentChannelPacket::encode,
                S2CCurrentChannelPacket::new,
                (msg, ctx) -> msg.handle(ctx)
        );
    }

    public static int nextId() {
        return nextPacketId++;
    }
}
