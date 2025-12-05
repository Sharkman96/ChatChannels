package com.example.chatchannels.network;

import com.example.chatchannels.server.chat.ServerChatHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSendMessagePacket {

    private final String channelId;
    private final String message;

    public C2SSendMessagePacket(String channelId, String message) {
        this.channelId = channelId;
        this.message = message;
    }

    public C2SSendMessagePacket(FriendlyByteBuf buf) {
        this.channelId = buf.readUtf(64);
        this.message = buf.readUtf(1024);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(channelId, 64);
        buf.writeUtf(message, 1024);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            ServerChatHandler.handleChannelMessage(player, channelId, message);
        });
        ctx.setPacketHandled(true);
    }

    public String getChannelId() {
        return channelId;
    }

    public String getMessage() {
        return message;
    }
}
