package com.example.chatchannels.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ChatChannelsServerConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue LOCAL_CHAT_RADIUS = BUILDER
            .comment("Default radius for the local chat channel")
            .defineInRange("localChat.radius", 100, 16, 1024);

    public static final ForgeConfigSpec.IntValue RATE_LIMIT_MESSAGES = BUILDER
            .comment("Maximum number of chat messages per player in the configured time window")
            .defineInRange("rateLimit.messages", 5, 1, 100);

    public static final ForgeConfigSpec.IntValue RATE_LIMIT_SECONDS = BUILDER
            .comment("Time window in seconds for the rate limiter")
            .defineInRange("rateLimit.seconds", 3, 1, 60);

    public static final ForgeConfigSpec.BooleanValue SHOW_CHANNEL_SWITCH_MESSAGES = BUILDER
            .comment("Show system messages when player joins/leaves chat channels")
            .define("channels.showSwitchMessages", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ChatChannelsServerConfig() {
    }
}
