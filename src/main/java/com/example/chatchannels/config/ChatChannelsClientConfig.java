package com.example.chatchannels.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ChatChannelsClientConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue SHOW_UNREAD_BADGE = BUILDER
            .comment("Show unread indicators on channels in the client UI")
            .define("ui.showUnreadBadge", true);

    public static final ForgeConfigSpec.BooleanValue PLAY_NOTIFICATION_SOUND = BUILDER
            .comment("Play a notification sound when a private message is received")
            .define("ui.playNotificationSound", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ChatChannelsClientConfig() {
    }
}
