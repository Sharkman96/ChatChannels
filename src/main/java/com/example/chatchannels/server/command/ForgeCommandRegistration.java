package com.example.chatchannels.server.command;

import com.example.chatchannels.ChatChannels;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChatChannels.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeCommandRegistration {

    private ForgeCommandRegistration() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ChatChannelsCommands.register(event.getDispatcher());
    }
}
