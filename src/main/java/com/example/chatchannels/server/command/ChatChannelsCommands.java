package com.example.chatchannels.server.command;

import com.example.chatchannels.server.channel.ChannelManager;
import com.example.chatchannels.server.chat.ServerChatHandler;
import com.example.chatchannels.server.perm.PermissionManager;
import com.example.chatchannels.server.player.PlayerChatStateManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ChatChannelsCommands {

    private ChatChannelsCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("channel")
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            var manager = ChannelManager.getInstance();
                            StringBuilder sb = new StringBuilder("Channels: ");
                            manager.getChannels().forEach(def -> {
                                String perm = def.getPermission();
                                if (perm != null && !perm.isEmpty()) {
                                    if (!PermissionManager.hasPermission(player, perm)) {
                                        return;
                                    }
                                }
                                if (!sb.toString().endsWith(" ")) {
                                    sb.append(", ");
                                }
                                sb.append(def.getId());
                            });
                            ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                            return 1;
                        }))
                .then(Commands.literal("join")
                        .then(Commands.argument("channel", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String channelId = StringArgumentType.getString(ctx, "channel");
                                    var manager = ChannelManager.getInstance();
                                    var def = manager.getChannel(channelId);
                                    if (def == null) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown channel: " + channelId));
                                        return 0;
                                    }
                                    PlayerChatStateManager.getInstance().setCurrentChannel(player, channelId);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Joined channel " + channelId), false);
                                    return 1;
                                })))
                .then(Commands.literal("leave")
                        .then(Commands.argument("channel", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String channelId = StringArgumentType.getString(ctx, "channel");
                                    // Для простоты: если покинули текущий канал, вернуться в дефолтный
                                    if (PlayerChatStateManager.getInstance()
                                            .getCurrentChannel(player, ChannelManager.getInstance().getDefaultChannelId())
                                            .equals(channelId)) {
                                        PlayerChatStateManager.getInstance().setCurrentChannel(player, ChannelManager.getInstance().getDefaultChannelId());
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal("Left channel " + channelId), false);
                                    return 1;
                                })))
        );

        dispatcher.register(Commands.literal("ch")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String msg = StringArgumentType.getString(ctx, "message");
                            ServerChatHandler.handleChannelMessage(player, "", msg);
                            return 1;
                        }))
        );

        dispatcher.register(Commands.literal("pm")
                .then(Commands.argument("target", StringArgumentType.word())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                                    String targetName = StringArgumentType.getString(ctx, "target");
                                    ServerPlayer target = sender.server.getPlayerList().getPlayerByName(targetName);
                                    if (target == null) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown player: " + targetName));
                                        return 0;
                                    }

                                    String msg = StringArgumentType.getString(ctx, "message");
                                    ServerChatHandler.handlePrivateMessage(sender, target, msg);
                                    return 1;
                                }))
                )
        );
    }
}
