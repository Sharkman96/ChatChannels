package com.example.chatchannels.network;

import com.example.chatchannels.client.ClientChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CCurrentChannelPacket {

    private final String channelId;

    public S2CCurrentChannelPacket(String channelId) {
        this.channelId = channelId;
    }

    public S2CCurrentChannelPacket(FriendlyByteBuf buf) {
        this.channelId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(channelId == null ? "" : channelId, 64);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ClientChannelManager.getInstance().setCurrentChannelId(channelId);
        ClientChannelManager.getInstance().redrawCurrentChannelChat();
    }
}
