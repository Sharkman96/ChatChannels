package com.example.chatchannels.config;

import com.example.chatchannels.ChatChannels;
import com.example.chatchannels.api.ChannelType;
import com.example.chatchannels.config.ChatChannelsServerConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChannelConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatChannels.MOD_ID + ":config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CONFIG_FILE_NAME = "chatchannels.json";

    private ChannelConfigLoader() {
    }

    public static ChannelConfig loadOrCreateDefault() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);

        if (Files.notExists(configPath)) {
            ChannelConfig defaults = createDefaultConfig();
            try {
                writeConfig(configPath, defaults);
            } catch (IOException e) {
                LOGGER.error("Failed to write default chat channels config", e);
            }
            return defaults;
        }

        try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            ChannelConfig config = GSON.fromJson(reader, ChannelConfig.class);
            if (config == null) {
                LOGGER.warn("ChatChannels config was empty, recreating defaults");
                config = createDefaultConfig();
            }
            return config;
        } catch (IOException e) {
            LOGGER.error("Failed to read chat channels config, using defaults", e);
            return createDefaultConfig();
        }
    }

    private static void writeConfig(Path path, ChannelConfig config) throws IOException {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
    }

    private static ChannelConfig createDefaultConfig() {
        ChannelConfig config = new ChannelConfig();

        // global
        var global = new com.example.chatchannels.api.ChannelDefinitionBuilder()
                .id("global")
                .name("Global")
                .type(ChannelType.GLOBAL)
                .prefix("[G]")
                .color("#FFFFFF")
                .radius(0)
                .isDefault(true)
                .permission("chat.channels.global")
                .build();

        // local
        var local = new com.example.chatchannels.api.ChannelDefinitionBuilder()
                .id("local")
                .name("Local")
                .type(ChannelType.LOCAL)
                .prefix("[Local]")
                .color("#CCCC99")
                .radius(ChatChannelsServerConfig.LOCAL_CHAT_RADIUS.get())
                .isDefault(false)
                .permission("chat.channels.local")
                .build();

        // trade
        var trade = new com.example.chatchannels.api.ChannelDefinitionBuilder()
                .id("trade")
                .name("Trade")
                .type(ChannelType.TRADE)
                .prefix("[Trade]")
                .color("#FFD700")
                .radius(0)
                .isDefault(false)
                .permission("chat.channels.trade")
                .build();

        // staff
        var staff = new com.example.chatchannels.api.ChannelDefinitionBuilder()
                .id("staff")
                .name("Staff")
                .type(ChannelType.STAFF)
                .prefix("[Staff]")
                .color("#FF5555")
                .radius(0)
                .isDefault(false)
                .permission("chat.channels.staff")
                .build();

        // private messages channel
        var pm = new com.example.chatchannels.api.ChannelDefinitionBuilder()
                .id("pm")
                .name("Private")
                .type(ChannelType.PRIVATE)
                .prefix("[PM]")
                .color("#55FFFF")
                .radius(0)
                .isDefault(false)
                .permission("")
                .build();

        config.channels.add(global);
        config.channels.add(local);
        config.channels.add(trade);
        config.channels.add(staff);
        config.channels.add(pm);

        config.rateLimit.messages = ChatChannelsServerConfig.RATE_LIMIT_MESSAGES.get();
        config.rateLimit.perSeconds = ChatChannelsServerConfig.RATE_LIMIT_SECONDS.get();

        return config;
    }
}
