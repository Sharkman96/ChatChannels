package com.example.chatchannels.network;

import com.example.chatchannels.api.ChannelDefinition;
import com.example.chatchannels.client.ClientChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CChannelListPacket {

    private final List<ChannelDefinition> channels;

    public S2CChannelListPacket(List<ChannelDefinition> channels) {
        this.channels = channels;
    }

    public S2CChannelListPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.channels = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String id = buf.readUtf(64);
            String name = buf.readUtf(64);
            String type = buf.readUtf(16);
            String prefix = buf.readUtf(64);
            String color = buf.readUtf(16);
            int radius = buf.readVarInt();
            boolean isDefault = buf.readBoolean();
            String permission = buf.readUtf(128);
            com.example.chatchannels.api.ChannelDefinition def =
                    new com.example.chatchannels.api.ChannelDefinition(
                            id,
                            name,
                            com.example.chatchannels.api.ChannelType.valueOf(type),
                            prefix,
                            color,
                            radius,
                            isDefault,
                            permission
                    );
            this.channels.add(def);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(channels.size());
        for (ChannelDefinition def : channels) {
            buf.writeUtf(def.getId(), 64);
            buf.writeUtf(def.getName(), 64);
            buf.writeUtf(def.getType().name(), 16);
            buf.writeUtf(def.getPrefix(), 64);
            buf.writeUtf(def.getColorHex(), 16);
            buf.writeVarInt(def.getRadius());
            buf.writeBoolean(def.isDefault());
            buf.writeUtf(def.getPermission() == null ? "" : def.getPermission(), 128);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ClientChannelManager.getInstance().setChannels(this.channels);
    }

    public List<ChannelDefinition> getChannels() {
        return channels;
    }
}
