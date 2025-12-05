package com.example.chatchannels.client;

import com.example.chatchannels.ChatChannels;
import com.example.chatchannels.network.C2SSendMessagePacket;
import com.example.chatchannels.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChatChannels.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChatChannelsClientChat {

    private ChatChannelsClientChat() {
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }

        // Оставляем команды ванилле
        if (message.startsWith("/")) {
            return;
        }

        // Не отправляем обычное сообщение на сервер напрямую
        event.setCanceled(true);

        String channelId = ClientChannelManager.getInstance().getCurrentChannelId();
        if (channelId == null) {
            channelId = ""; // сервер сам подставит дефолтный канал
        }

        NetworkHandler.CHANNEL.sendToServer(new C2SSendMessagePacket(channelId, message));
    }
}
