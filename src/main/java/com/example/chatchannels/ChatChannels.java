package com.example.chatchannels;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import com.example.chatchannels.config.ChatChannelsClientConfig;
import com.example.chatchannels.config.ChatChannelsServerConfig;
import com.example.chatchannels.config.ChannelConfig;
import com.example.chatchannels.config.ChannelConfigLoader;
import com.example.chatchannels.config.PrivilegeColorConfigLoader;
import com.example.chatchannels.network.NetworkHandler;
import com.example.chatchannels.server.channel.ChannelManager;
import com.example.chatchannels.server.log.ChatLogManager;
import com.example.chatchannels.server.ratelimit.RateLimiterManager;

@Mod(ChatChannels.MOD_ID)
public class ChatChannels {

    public static final String MOD_ID = "chatchannels";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ChatChannels(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        context.registerConfig(ModConfig.Type.SERVER, ChatChannelsServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ChatChannelsClientConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.init();
            ChannelConfig config = ChannelConfigLoader.loadOrCreateDefault();
            ChannelManager.getInstance().loadFromConfig(config);
            RateLimiterManager.getInstance().configure(config);
            ChatLogManager.init();
            PrivilegeColorConfigLoader.loadOrCreate();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Клиентская инициализация (UI, хоткеи) будет добавлена позже
        LOGGER.debug("ChatChannels client setup");
    }
}
