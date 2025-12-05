package com.example.chatchannels.server.player;

import com.example.chatchannels.ChatChannels;
import com.example.chatchannels.network.NetworkHandler;
import com.example.chatchannels.network.S2CCurrentChannelPacket;
import com.example.chatchannels.server.channel.ChannelManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ChatChannels.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChatChannelsServerPlayerEvents {

    private ChatChannelsServerPlayerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ChannelManager manager = ChannelManager.getInstance();
        String defaultId = manager.getDefaultChannelId();
        if (defaultId == null || defaultId.isEmpty()) {
            return;
        }
        PlayerChatStateManager.getInstance().setCurrentChannel(player, defaultId);
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CCurrentChannelPacket(defaultId));
    }
}
